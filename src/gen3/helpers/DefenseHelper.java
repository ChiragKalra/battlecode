package gen3.helpers;

import battlecode.common.*;

import static gen3.RobotPlayer.*;
import static gen3.Politician.*;

public class DefenseHelper {

	public static boolean onWall(MapLocation ml) throws GameActionException {
		return contains(innerPoints, ml) || contains(outerPoints, ml);
	}

	public static boolean isTunnelPoint(MapLocation ml) {
		return ml.x == spawnerLocation.x || ml.y == spawnerLocation.y || 
				Math.abs(ml.x - spawnerLocation.x) == Math.abs(ml.y - spawnerLocation.y);
	}

	public static boolean contains(MapLocation[] mapLocations, MapLocation ml) throws GameActionException {
		for (MapLocation mapLocation : mapLocations) {
			if (mapLocation.equals(ml)) {
				return true;
			}
		}

		return false;
	}

	public static boolean tryMoveWall(Direction dir) throws GameActionException {
		MapLocation ml = rc.getLocation().add(dir);
		if (outsideWall(ml)) {
			return false;
		}

    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }

    public static boolean outsideWall(MapLocation ml) {
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
