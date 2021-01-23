package gen5;

import battlecode.common.*;
import gen5.util.EcInfo;
import gen5.util.Pair;
import gen5.util.SpawnType;

import static gen5.RobotPlayer.*;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.MovementHelper.*;

public strictfp class AttackPolitician {

    public static void move() throws GameActionException {
        if (rc.getConviction() < SpawnType.AttackPolitician.minHp) {
            for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, mTeam)) {
                if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    enlightenmentCenterId = ri.getID();
                    spawnerLocation = ri.location;
                    spawnType = SpawnType.DefensePolitician;
                    DefensePolitician.init();
                    DefensePolitician.move();
                    return;
                }
            }
        }


        int rad = shouldAttackOffensive();
        if (rad != 0) {
            rc.empower(rad);
            return;
        }

        if (roundNumber - roundSpawned > 750) {
            Pair<Integer, Boolean> got = shouldAttackDefensive();
            if (got.key != 0) {
                rc.empower(got.key);
                return;
            }
        }

        MapLocation locToEmp = getOptimalLocationToEmpower();
        if (shouldSelfEmpower()) {
            locToEmp = spawnerLocation;
        }

        if (locToEmp != null) {
            goTo(locToEmp);
        } else {
            EcInfo got = checkForAttackCoordinates();
            if (got != null && got.hp >= 0) {
                goTo(got.location);
            } else {
                tryMove(getNextDirection(null), false);
            }
        }
    }


    public static void init() { }
}
