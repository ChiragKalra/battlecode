package gen3;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen3.util.Pair;
import gen3.util.SpawnType;

import static gen3.RobotPlayer.rc;
import static gen3.helpers.AttackHelper.*;
import static gen3.helpers.MovementHelper.*;


public strictfp class Politician {
    public static boolean isAttackType = false;

    public static void init() {
        isAttackType = rc.getInfluence() > SpawnType.DefensePolitician.maxHp;
    }


    private static MapLocation locToEmp = null;
    private static void moveOffense () throws GameActionException {
        if (shouldAttackOffensive()) {
            rc.empower(1);
        } else {
            if (locToEmp == null) {
                locToEmp = getOptimalLocationToEmpower();
            }
            if (locToEmp != null) {
                tryMove(rc.getLocation().directionTo(locToEmp));
            } else {
                Pair<MapLocation, Integer> got = checkForAttackCoordinates();
                if (got != null) {
                    MapLocation attackLocation = got.key;
                    if (attackLocation == null || tryMove(rc.getLocation().directionTo(attackLocation))) {
                        tryMove(getNextDirection(attackLocation));
                    }
                }
            }
        }
    }

    private static void moveDefense () throws GameActionException {
        int rad = shouldAttackDefensive();
        if (rad != 0) {
            rc.empower(rad);
        } else {
            // TODO Keep wall
            tryMove(getRandomDirection());
        }
    }

    public static void move() throws GameActionException {
        if (rc.isReady()) {
            if (isAttackType) {
               moveOffense();
            } else {
                moveDefense();
            }
        }
    }
}
