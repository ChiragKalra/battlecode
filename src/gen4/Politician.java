package gen4;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen4.util.Pair;
import gen4.util.SpawnType;

import static gen4.RobotPlayer.*;
import static gen4.flags.EnlightenmentCenterFlag.getRadius;
import static gen4.helpers.AttackHelper.*;
import static gen4.helpers.DefenseHelper.*;
import static gen4.helpers.MovementHelper.*;

public strictfp class Politician {
    public static boolean isAttackType = false;

    private static MapLocation locToEmp = null;

    public static void moveAttack() throws GameActionException {
        int rad = shouldAttackOffensive();
        if (rad != 0) {
            rc.empower(rad);
            return;
        }

        // movement
        if (locToEmp == null) {
            locToEmp = getOptimalLocationToEmpower();
        }
        if (locToEmp != null) {
            tryMove(rc.getLocation().directionTo(locToEmp));
        } else {
            Pair<MapLocation, Integer> got = checkForAttackCoordinates();
            if (got != null && got.value >= 0) {
                MapLocation attackLocation = got.key;
                if (attackLocation == null || tryMove(rc.getLocation().directionTo(attackLocation))) {
                    tryMove(getNextDirection(attackLocation));
                }
            } else {
                tryMove(getNextDirection(null));
            }
        }
    }

    private static int innerRadius, outerRadius;

    public static void moveDefense () throws GameActionException {
        int rad = shouldAttackDefensive();
        if (rad != 0) {
            rc.empower(rad);
            return;
        } else {
            rad = shouldAttackOffensive();
            if (rad != 0) {
                rc.empower(rad);
                return;
            }
        }

        int radius;
        if (rc.canGetFlag(enlightenmentCenterId)) {
            radius = getRadius(rc.getFlag(enlightenmentCenterId));
        } else {
            isAttackType = true;
            moveAttack();
            return;
        }

        innerRadius = radius * radius + 1;
        outerRadius = (radius + 1) * (radius + 1) + 1;

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

        if (onWall(rc.getLocation(), innerRadius, outerRadius)) {
            if (!isTunnelPoint(rc.getLocation())) {
                return;
            }
            
            if (forceMove(left.rotateLeft())) {
                return;
            }
            if (forceMove(right.rotateRight())) {
                return;
            }
            if (forceMove(left)) {
                return;
            }
            if (forceMove(right)) {
                return;
            }
            if (forceMove(straight)) {
                return;
            }

            return;
        }

        if (outsideWall(rc.getLocation(), outerRadius)) {
            Direction opposite = straight.opposite();
            Direction oppLeft = opposite.rotateLeft();
            Direction oppRight = opposite.rotateRight();

            if (forceMove(oppRight)) {
                return;
            }
            if (forceMove(oppLeft)) {
                return;
            }
            if (forceMove(oppRight.rotateRight())) {
                return;
            }
            if (forceMove(oppLeft.rotateLeft())) {
                return;
            }

            if (isTunnelPoint(rc.getLocation())) {
                if (forceMove(left)) {
                    return;
                }
                if (forceMove(right)) {
                    return;
                }
                if (forceMove(straight)) {
                    return;
                }
            } else if (forceMove(opposite)) {
                return;
            }

            return;
        }

        if (tryMoveWall(straight, outerRadius)) {
            return;
        }
        if (tryMoveWall(left, outerRadius)) {
            return;
        }
        if (tryMoveWall(right, outerRadius)) {
            return;
        }

        for (int i = 0; i < 2; ++i) {
            left = left.rotateLeft();
            if (tryMoveWall(left, outerRadius)) {
                return;
            }

            right = right.rotateRight();
            if (tryMoveWall(right, outerRadius)) {
                return;
            }
        }

        left = left.rotateLeft();
        tryMoveWall(left, outerRadius);
    }

    public static void move() throws GameActionException {
        if (rc.isReady()) {

            // move modes
            if (isAttackType) {
                moveAttack();
            } else {
                moveDefense();
            }
        }
    }

    public static void init() {
        isAttackType = rc.getInfluence() >= SpawnType.AttackPolitician.minHp;
    }
}
