package gen5.helpers;

import battlecode.common.*;
import gen5.util.SpawnType;

import static gen5.RobotPlayer.*;

public class DefenseHelper {

	public static Direction tunnelShift = Direction.CENTER;

	private static int roundCached = 0;
	private static RobotInfo[] nearby;

	public static boolean isTunnelPoint(MapLocation ml) {
		if (haveWonInVotes) {
			return false;
		}

		if (roundCached < roundNumber || nearby == null) {
			nearby = rc.senseNearbyRobots(ml, 5, null);
			roundCached = roundNumber;
		}
		for (RobotInfo info: nearby) {
			if (info.type == RobotType.ENLIGHTENMENT_CENTER && spawnerLocation != info.location) {
				return true;
			}
		}
		// check if it's a grid location, allow the grid pols to occupy
		if (GridHelper.formsGrid()) return true;

		if (spawnerLocation == null) {
			return false;
		}
		return ml.x == spawnerLocation.x + tunnelShift.dx || ml.y == spawnerLocation.y + tunnelShift.dy;
	}

	public static boolean forceMoveWall(Direction dir) throws GameActionException {
		if (!isTunnelPoint(rc.getLocation().add(dir)) && rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}
		return false;
	}

	public static boolean tryMoveWall(Direction dir, int outerRadius) throws GameActionException {
		MapLocation ml = rc.getLocation().add(dir);
		if (outsideWall(ml, outerRadius)) {
			return false;
		}

    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }

	public static boolean onWall(MapLocation ml, int innerRadius, int outerRadius) {
		if (spawnerLocation == null) {
			return false;
		}
		return isWallOfRadius(spawnerLocation, ml, innerRadius) || isWallOfRadius(spawnerLocation, ml, outerRadius);
	}

	public static boolean isWallOfRadius(MapLocation center, MapLocation ml, int rs) {
		if (center == null || ml == null) {
			return false;
		}
		if (!center.isWithinDistanceSquared(ml, rs)) return false;
		int x = Math.abs(ml.x-center.x), y = (int) Math.sqrt(rs-x*x);
		int abs = Math.abs(ml.y - center.y);
		if (y == abs) return true;
		return (x*x + 2*x + 1) + abs*abs > rs && y > abs;
	}

    public static boolean outsideWall(MapLocation ml, int outerRadius) {
    	if (spawnerLocation == null) {
    		return false;
    	}
    	return ml.distanceSquaredTo(spawnerLocation) > outerRadius;
    }

    public static boolean forceMove (Direction dir) throws GameActionException {
    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }

    public static Direction getApproachDirection () {
		boolean detectedSlanderer = false;
		int minHp = rc.getConviction(), friendRad = actionRadius+1, enemyRadius = actionRadius+1, mHp = minHp;
		MapLocation current = rc.getLocation(), average = new MapLocation(0,0);
		for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius)) {
			if (ri.type != RobotType.ENLIGHTENMENT_CENTER &&
					(ri.conviction <= (mHp-10)*6 || rc.getInfluence() == SpawnType.DefensePolitician.maxHp)) {
				if (ri.team == enemyTeam) {
					enemyRadius = Math.min(current.distanceSquaredTo(ri.location), enemyRadius);
					average = average.translate(ri.location.x-current.x, ri.location.y-current.y);
					if (ri.location.isWithinDistanceSquared(current, 1)) {
						return null;
					}
				} else {
					if (!detectedSlanderer) {
						detectedSlanderer = ri.type == RobotType.SLANDERER;
					}
					friendRad = Math.min(current.distanceSquaredTo(ri.location), friendRad);
					if (ri.type == RobotType.POLITICIAN) {
						minHp = Math.min(minHp, ri.getConviction());
					}
				}
			}
		}
		if (!detectedSlanderer) {
			return null;
		}
		if (minHp != rc.getConviction()) {
			return null;
		}
		if (friendRad > enemyRadius) {
			return null;
		}

		return (new MapLocation(0, 0)).directionTo(average);
	}
}
