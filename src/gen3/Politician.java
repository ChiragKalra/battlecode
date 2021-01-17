package gen3;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static gen3.RobotPlayer.*;
import static gen3.flags.EnlightenmentCenterFlag.getAttackCoordinates;
import static gen3.flags.EnlightenmentCenterFlag.isAttackType;
import static gen3.helpers.AttackHelper.*;
import static gen3.helpers.MovementHelper.Precision;
import static gen3.helpers.MovementHelper.tryMove;


public strictfp class Politician {
    public static MapLocation attackLocation = null;
    public static boolean isAttackType = false;

    public static void init() throws GameActionException {
        isAttackType = isAttackType(rc.getFlag(enlightenmentCenterId));
    }

    public static void checkForAttackCoordinates() throws GameActionException {
        int flag = rc.getFlag(enlightenmentCenterId);
        if (isAttackType) {
            attackLocation = getAttackCoordinates(flag);
            if (targetAlreadyCaptured(attackLocation)) {
                attackLocation = null;
            }
        }
    }

    public static void move() throws GameActionException {
        checkForAttackCoordinates();

        if (attackLocation != null) {
            if (targetAlreadyCaptured(attackLocation)) {
                attackLocation = null;
            }
        }
        if (rc.isReady()) {
            if (shouldAttack()) {
                rc.empower(actionRadius);
            } else {
                Direction bo = shouldBackOff();
                if (bo != null) {
                    tryMove(bo, Precision.MIN);
                } else {
                    Direction next = getNextDirection(rc.getLocation(), attackLocation);
                    tryMove(next, Precision.MIN);
                }
            }
        }
    }
}
