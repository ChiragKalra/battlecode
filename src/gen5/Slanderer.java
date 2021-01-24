package gen5;

import battlecode.common.*;
import gen5.helpers.MovementHelper;

import static gen5.RobotPlayer.*;
import static gen5.flags.EnlightenmentCenterFlag.getRadius;
import static gen5.flags.EnlightenmentCenterFlag.getShiftDirection;
import static gen5.helpers.DefenseHelper.*;
import static gen5.helpers.FarmHelper.*;

public strictfp class Slanderer {
    private static final int RADIUS_CAP = 7;

    public static void move() throws GameActionException {
        if (!rc.isReady()) {
            return;
        }

        if (spawnerLocation == null) {
            MovementHelper.tryMove(MovementHelper.getRandomDirection(), false);
            return;
        }

        int radius;
        if (rc.canGetFlag(enlightenmentCenterId)) {
            radius = getRadius(rc.getFlag(enlightenmentCenterId));
        } else {
            Direction opposite = getAntiEcOrMuckDirection();
            MovementHelper.tryMove(opposite, false);
            return;
        }

        int innerRadius = radius * radius + 1;
        int outerRadius = (radius + 1) * (radius + 1) + 1;

        Direction antiMuck = getAntiMuckDirection();
        if (antiMuck != null) {
            tryRun(antiMuck, innerRadius, outerRadius);
            return;
        }

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

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

            return;
        }

        if (radius > RADIUS_CAP) {
            radius = RADIUS_CAP;
            innerRadius = radius * radius + 1;
            outerRadius = (radius + 1) * (radius + 1) + 1;
            if (onWall(rc.getLocation(), innerRadius, outerRadius) || outsideWall(rc.getLocation(), outerRadius)) {
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

                return;
            }
        }

        boolean nearWall = onWall(rc.getLocation().add(straight), innerRadius, outerRadius) ||
                onWall(rc.getLocation().add(left), innerRadius, outerRadius) ||
                onWall(rc.getLocation().add(right), innerRadius, outerRadius);
        if (nearWall) {
            if (isTunnelPoint(rc.getLocation())) {
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

    public static void init() throws GameActionException {
        if (rc.canGetFlag(enlightenmentCenterId)) {
            tunnelShift = getShiftDirection(rc.getFlag(enlightenmentCenterId));
        }
    }
}
