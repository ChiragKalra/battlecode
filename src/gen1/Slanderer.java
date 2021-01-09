package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.MovementHelper.*;


public strictfp class Slanderer {

    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

     static void move() throws GameActionException {
         if (tryMove(getRandomDirection()))
             System.out.println("I moved!");
    }
}
