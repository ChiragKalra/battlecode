package gen3.helpers;

import battlecode.common.*;

import static gen3.RobotPlayer.*;
import static gen3.Slanderer.*;

public class FarmHelper {

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
}
