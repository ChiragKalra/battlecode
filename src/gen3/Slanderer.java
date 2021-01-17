package gen3;

import battlecode.common.*;

import static gen3.helpers.MovementHelper.getRandomDirection;
import static gen3.helpers.MovementHelper.tryMove;

public strictfp class Slanderer {

    public static void move() throws GameActionException {
        tryMove(getRandomDirection());
    }
}
