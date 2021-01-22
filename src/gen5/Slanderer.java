package gen5;

import battlecode.common.*;
import gen5.helpers.MovementHelper;

import static gen5.RobotPlayer.*;
import static gen5.flags.EnlightenmentCenterFlag.getRadius;
import static gen5.flags.EnlightenmentCenterFlag.getShiftDirection;
import static gen5.helpers.DefenseHelper.*;


public strictfp class Slanderer {
    private static int RADIUS_CAP = 6;

    private static Direction getAntiMuckDirection() {
        // TODO: move away from muckrakers (optimise)
        int[] dirOcc = new int[8];
        boolean found = false;
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                found = true;
                dirOcc[MovementHelper.directionList.indexOf(ri.location.directionTo(rc.getLocation()))]++;
            }
        }

        int ind  = 0;
        for (int i = 1; i < 8; i++) {
            if (dirOcc[i] > dirOcc[ind]) {
                ind = i;
            }
        }

        if (found) {
            return MovementHelper.directions[ind];
        }
        return null;
    }

    public static void move() throws GameActionException {
        if (!rc.isReady()) {
            return;
        }

        Direction antiMuck = getAntiMuckDirection();
        if (antiMuck != null) {
            MovementHelper.tryMove(antiMuck, false);
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

        log("No muck");

        if (radius > RADIUS_CAP)
            radius = RADIUS_CAP;
        int innerRadius = radius * radius + 1;
        int outerRadius = (radius + 1) * (radius + 1) + 1;
        log("radius: " + radius + " " + innerRadius + " " + outerRadius);

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

        log("Not on wall");

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

        log("Not outside wall");

        boolean nearWall = onWall(rc.getLocation().add(straight), innerRadius, outerRadius) ||
                onWall(rc.getLocation().add(left), innerRadius, outerRadius) ||
                onWall(rc.getLocation().add(right), innerRadius, outerRadius);
        if (nearWall) {
            if (isTunnelPoint(rc.getLocation())) {
                System.out.println(rc.getLocation().x + ' ' + rc.getLocation().y);
                System.out.println(isTunnelPoint(rc.getLocation()));
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

            log("Near wall and not a tunnel point");

            return;
        }

        log("Not near wall");

        if (forceMoveWall(straight)) {
            return;
        }
        if (forceMoveWall(left)) {
            return;
        }
        if (forceMoveWall(right)) {
            return;
        }

        log("Couldn't move ahead");

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
