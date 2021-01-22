package gen5;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen5.util.Pair;
import gen5.util.SpawnType;

import static gen5.RobotPlayer.*;
import static gen5.flags.EnlightenmentCenterFlag.getRadius;
import static gen5.flags.EnlightenmentCenterFlag.getShiftDirection;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.DefenseHelper.*;
import static gen5.helpers.MovementHelper.*;

public strictfp class AttackPolitician {
    private static MapLocation locToEmp = null;

    public static void move() throws GameActionException {
        int rad = shouldAttackOffensive();
        if (rad != 0) {
            rc.empower(rad);
            return;
        }

        if (shouldSelfEmpower()) {
            locToEmp = spawnerLocation;
        }

        // movement
        if (locToEmp == null) {
            locToEmp = getOptimalLocationToEmpower();
        }
        if (locToEmp != null) {
            goTo(locToEmp);
        } else {
            Pair<MapLocation, Integer> got = checkForAttackCoordinates();
            if (got != null && got.value >= 0) {
                goTo(got.key);
            } else {
                tryMove(getNextDirection(null), false);
            }
        }
    }


    public static void init() throws GameActionException { }
}
