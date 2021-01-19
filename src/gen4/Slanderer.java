package gen4;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static gen4.RobotPlayer.*;
import static gen4.flags.EnlightenmentCenterFlag.getRadius;
import static gen4.helpers.DefenseHelper.isTunnelPoint;
import static gen4.helpers.DefenseHelper.onWall;
import static gen4.helpers.FarmHelper.*;
import static gen4.helpers.MovementHelper.getCircumferencePoints;

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

        boolean onWallStraight = onWall(rc.getLocation().add(straight));
        boolean onWallLeft = onWall(rc.getLocation().add(left));
        boolean onWallRight = onWall(rc.getLocation().add(right));

        boolean nearWall = false;
        if (onWallStraight || onWallLeft || onWallRight) {
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

        if (!onWallStraight && tryMoveSlanderer(straight)) {
            return;
        }
        if (!onWallLeft && tryMoveSlanderer(left)) {
            return;
        }
        if (!onWallRight && tryMoveSlanderer(right)) {
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
        // if (onWall(rc.getLocation().add(dir))) {
        //     return false;
        // }
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }
}
