package gen3;

import battlecode.common.*;
import gen3.flags.MuckrakerFlag;
import gen3.util.Pair;

import static gen3.RobotPlayer.*;
import static gen3.flags.EnlightenmentCenterFlag.isAttackType;
import static gen3.flags.MuckrakerFlag.getHpFromFlag;
import static gen3.flags.MuckrakerFlag.isBroadcastingEC;
import static gen3.helpers.AttackHelper.*;
import static gen3.helpers.MovementHelper.*;


public strictfp class Politician {
    public static boolean isAttackType = false;

    public static void init() throws GameActionException {
        isAttackType = isAttackType(rc.getFlag(enlightenmentCenterId));
    }

    public static void move() throws GameActionException {
        if (rc.isReady()) {
            if (shouldAttack()) {
                rc.empower(actionRadius);
            } else if (isAttackType) {
                Direction bo = shouldBackOff();
                if (bo != null) {
                    tryMove(bo);
                } else {
                    Pair<MapLocation, Integer> got = checkForAttackCoordinates();
                    if (got != null) {
                        MapLocation attackLocation = got.key;
                        if (attackLocation == null || tryMove(rc.getLocation().directionTo(attackLocation))) {
                            tryMove(getNextDirection(attackLocation));
                        }
                    }
                }
            } else {
                tryMove(getRandomDirection());
            }
        }
    }
}
