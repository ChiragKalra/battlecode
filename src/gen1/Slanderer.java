package gen1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import static gen1.RobotPlayer.directions;
import static gen1.RobotPlayer.rc;


public strictfp class Slanderer {

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

     static void move() throws GameActionException {
         if (tryMove(randomDirection()))
             System.out.println("I moved!");
    }
}
