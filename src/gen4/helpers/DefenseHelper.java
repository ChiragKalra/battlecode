package gen4.helpers;

import battlecode.common.*;

import static gen4.RobotPlayer.rc;
import static gen4.RobotPlayer.spawnerLocation;

public class DefenseHelper {

	public static boolean isTunnelPoint(MapLocation ml) {
		return ml.x == spawnerLocation.x || ml.y == spawnerLocation.y;
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

	public static boolean onWall(MapLocation ml, int innerRadius, int outerRadius) throws GameActionException {
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
