package gen5;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import gen5.helpers.AttackHelper;
import gen5.util.Pair;
import gen5.util.SpawnType;

import static gen5.RobotPlayer.*;
import static gen5.flags.EnlightenmentCenterFlag.getRadius;
import static gen5.flags.EnlightenmentCenterFlag.getShiftDirection;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.DefenseHelper.*;
import static gen5.helpers.MovementHelper.*;

public strictfp class DefensePolitician {

    public static void move() throws GameActionException {
        /*for (RobotInfo ri: rc.senseNearbyRobots(actionRadius, mTeam)) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                enlightenmentCenterId = ri.getID();
            }
        }
*/
        int radius;
        if (rc.canGetFlag(enlightenmentCenterId)) {
            radius = getRadius(rc.getFlag(enlightenmentCenterId));
        } else {
            spawnType = SpawnType.AttackPolitician;
            AttackPolitician.move();
            return;
        }

        int innerRadius = radius * radius + 1;
        int outerRadius = (radius + 1) * (radius + 1) + 1;

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

        boolean onInnerWall = isWallOfRadius(spawnerLocation, rc.getLocation(), innerRadius);
        boolean onOuterWall = isWallOfRadius(spawnerLocation, rc.getLocation(), outerRadius);
        boolean isOnWall = onInnerWall || onOuterWall;
        boolean isOutsideWall = outsideWall(rc.getLocation(), outerRadius);

        int rad = shouldAttackDefensive();
        if (rad != 0 && spawnType == SpawnType.DefensePolitician) {
            // only >= third layer will explode normally
            if (isOutsideWall) {
                rc.empower(rad);
                return;
            }

            // second layer will explode if there's no third layer
            // for ()
            // first layer will explode if there's no second layer
            // check if third layer not present
        }

        if (isOnWall) {
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

        if (isOutsideWall) {
            Direction opposite = straight.opposite();
            Direction oppLeft = opposite.rotateLeft();
            Direction oppRight = opposite.rotateRight();

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
            } else if (forceMoveWall(opposite)) {
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

    public static void init() throws GameActionException {
        if (rc.canGetFlag(enlightenmentCenterId)) {
            tunnelShift = getShiftDirection(rc.getFlag(enlightenmentCenterId));
        }
    }
}
