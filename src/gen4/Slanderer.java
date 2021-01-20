package gen4;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import static gen4.RobotPlayer.*;
import static gen4.flags.EnlightenmentCenterFlag.getRadius;
import static gen4.helpers.DefenseHelper.*;


public strictfp class Slanderer {
    public static int innerRadius, outerRadius;

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
            // TODO: move away from muckrakers
            Direction opposite = rc.getLocation().directionTo(spawnerLocation).opposite();
            Direction oppLeft = opposite.rotateLeft();
            Direction oppRight = opposite.rotateRight();
            if (forceMove(opposite)) {
                return;
            }
            if (forceMove(oppLeft)) {
                return;
            }
            if (forceMove(oppRight)) {
                return;
            }
            if (forceMove(oppLeft.rotateLeft())) {
                return;
            }
            if (forceMove(oppRight.rotateRight())) {
                return;
            }

            return;
        }

        innerRadius = radius * radius + 1;
        outerRadius = (radius + 1) * (radius + 1) + 1;

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

        if (onWall(rc.getLocation(), innerRadius, outerRadius)) {
            Direction opposite = rc.getLocation().directionTo(spawnerLocation);
            Direction oppLeft = opposite.rotateLeft();
            Direction oppRight = opposite.rotateRight();
            if (forceMoveWall(opposite)) {
                return;
            }
            if (forceMoveWall(oppRight)) {
                return;
            }
            if (forceMoveWall(oppLeft)) {
                return;
            }
            if (forceMoveWall(oppRight.rotateRight())) {
                return;
            }
            if (forceMoveWall(oppLeft.rotateLeft())) {
                return;
            }

            /*if (isTunnelPoint(rc.getLocation())) {
                if (forceMove(straight)) {
                    return;
                }
                if (forceMove(left)) {
                    return;
                }
                if (forceMove(right)) {
                    return;
                }
            }*/

            return;
        }

        if (outsideWall(rc.getLocation(), outerRadius)) {
            if (forceMove(straight)) {
                return;
            }
            if (forceMove(left)) {
                return;
            }
            if (forceMove(right)) {
                return;
            }

            return;
        }

        boolean nearWall = onWall(rc.getLocation().add(straight), innerRadius, outerRadius) ||
                onWall(rc.getLocation().add(left), innerRadius, outerRadius) ||
                onWall(rc.getLocation().add(right), innerRadius, outerRadius);
        if (nearWall) {
            if (isTunnelPoint(rc.getLocation())) {
                for (int i = 0; i < 2; ++i) {
                    left = left.rotateLeft();
                    if (forceMove(left)) {
                        return;
                    }

                    right = right.rotateRight();
                    if (forceMove(right)) {
                        return;
                    }
                }

                if (forceMoveWall(straight.rotateLeft())) {
                    return;
                }
                if (forceMoveWall(straight.rotateRight())) {
                    return;
                }
                if (forceMoveWall(straight.opposite())) {
                    return;
                }
            }

            return;
        }

        if (forceMoveWall(straight)) {
            return;
        }
        if (forceMoveWall(left)) {
            return;
        }
        if (forceMoveWall(right)) {
            return;
        }

        for (int i = 0; i < 2; ++i) {
            left = left.rotateLeft();
            if (forceMoveWall(left)) {
                return;
            }

            right = right.rotateRight();
            if (forceMoveWall(right)) {
                return;
            }
        }
    }
}
