package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.MovementHelper.*;


public strictfp class Slanderer {

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

     static void move() throws GameActionException {
         tryMove(getRandomDirection());
    }
}
