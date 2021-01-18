package gen3;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static gen3.RobotPlayer.*;
import static gen3.flags.EnlightenmentCenterFlag.getRadius;
import static gen3.helpers.FarmHelper.*;
import static gen3.helpers.MovementHelper.getCircumferencePoints;

public strictfp class Slanderer {
	private static final int WALL_RADIUS_MIN = 18;
	private static final int WALL_RADIUS_MAX = 30;

	public static int innerRadius, outerRadius;
    public static MapLocation[] innerPoints, outerPoints;

    public static void move() throws GameActionException {
        if (!rc.isReady()) {
        	return;
        }

        if (spawnerLocation == null) {
            return;
        }

        int radius;
        if (rc.canGetFlag(enlightenmentCenterId)) {
            radius = getRadius(rc.getFlag(enlightenmentCenterId));
        } else {
            return;
        }
        
        innerRadius = radius * radius + 1;
        outerRadius = (radius + 1) * (radius + 1) + 1;

        innerPoints = getCircumferencePoints(spawnerLocation, innerRadius);
        outerPoints = getCircumferencePoints(spawnerLocation, outerRadius);

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

        if (tryMoveSlanderer(straight)) {
            return;
        }
        if (tryMoveSlanderer(left)) {
            return;
        }
        if (tryMoveSlanderer(right)) {
            return;
        }

        boolean nearWall = false;
        if (onWall(rc.getLocation().add(straight)) || onWall(rc.getLocation().add(left)) || 
            onWall(rc.getLocation().add(right))) {
            nearWall = true;
        }

        if (nearWall) {
            if (isTunnelPoint(rc.getLocation())) {
                for (int i = 0; i < 2; ++i) {
                    left = left.rotateLeft();
                    if (tryMoveSlanderer(left)) {
                        return;
                    }

                    right = right.rotateRight();
                    if (tryMoveSlanderer(right)) {
                        return;
                    }
                }
            }

            return;
        }

        for (int i = 0; i < 2; ++i) {
            left = left.rotateLeft();
            if (tryMoveSlanderer(left)) {
                return;
            }

            right = right.rotateRight();
            if (tryMoveSlanderer(right)) {
                return;
            }
        }
    }

    private static boolean tryMoveSlanderer(Direction dir) throws GameActionException {
        if (onWall(rc.getLocation().add(dir))) {
            return false;
        }
    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }
}
