package gen1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

import static gen1.RobotPlayer.directions;
import static gen1.RobotPlayer.rc;

public strictfp class Muckraker {

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
         Team enemy = rc.getTeam().opponent();
         int actionRadius = rc.getType().actionRadiusSquared;
         for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
             if (robot.type.canBeExposed()) {
                 // It's a slanderer... go get them!
                 if (rc.canExpose(robot.location)) {
                     System.out.println("e x p o s e d");
                     rc.expose(robot.location);
                     return;
                 }
             }
         }
         if (tryMove(randomDirection()))
             System.out.println("I moved!");
    }
}
