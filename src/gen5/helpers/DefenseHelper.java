package gen5.helpers;

import battlecode.common.*;

import static gen5.RobotPlayer.*;

public class DefenseHelper {

	public static Direction tunnelShift = Direction.CENTER;

	private static int roundCached = 0;
	private static RobotInfo[] nearby;

	public static boolean isTunnelPoint(MapLocation ml) {
		if (roundCached < roundNumber || nearby == null) {
			nearby = rc.senseNearbyRobots(2);
			roundCached = roundNumber;
		}
		for (RobotInfo info: nearby) {
			if (info.type == RobotType.ENLIGHTENMENT_CENTER && spawnerLocation != info.location) {
				return true;
			}
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

	public static boolean onWall(MapLocation ml, int innerRadius, int outerRadius)  {
		return isWallOfRadius(spawnerLocation, ml, innerRadius) || isWallOfRadius(spawnerLocation, ml, outerRadius);
	}

	public static boolean isWallOfRadius(MapLocation center, MapLocation ml, int rs) {
		if (!center.isWithinDistanceSquared(ml, rs)) return false;
		int x = Math.abs(ml.x-center.x), y = (int) Math.sqrt(rs-x*x);
		int abs = Math.abs(ml.y - center.y);
		if (y == abs) return true;
		return (x*x + 2*x + 1) + abs*abs > rs && y > abs;
	}

    public static boolean outsideWall(MapLocation ml, int outerRadius) {
    	return ml.distanceSquaredTo(spawnerLocation) > outerRadius;
    }

    public static boolean forceMove(Direction dir) throws GameActionException {
    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }
}
