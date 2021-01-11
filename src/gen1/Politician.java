package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.MovementHelper.*;
import static gen1.helpers.AttackHelper.*;


public strictfp class Politician {

    public static void move() throws GameActionException {
        if (shouldAttack()) {
            rc.empower(actionRadius);
        }

        if (rc.getCooldownTurns() < 1) {
            tryMove(getNextDirection(rc.getLocation()), Precision.MIN);
        }
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {

    }
}
