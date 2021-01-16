package gen2;

import battlecode.common.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.helpers.AttackHelper.*;
import static gen2.flags.EnlightenmentCenterFlag.*;


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
            Direction bo = shouldBackOff();
            if (bo != null) {
                tryMove(bo, Precision.MIN);
            } else if (shouldAttack()) {
                rc.empower(actionRadius);
            } else {
                Direction next = getNextDirection(rc.getLocation(), attackLocation);
                tryMove(next, Precision.MIN);
            }
        }
    }
}
