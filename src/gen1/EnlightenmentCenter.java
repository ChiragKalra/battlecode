package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;

public strictfp class EnlightenmentCenter {

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

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

    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

     static void move() throws GameActionException {
         RobotType toBuild = randomSpawnableRobotType();
         int influence = 50;
         for (Direction dir : directions) {
             if (rc.canBuildRobot(toBuild, dir, influence)) {
                 rc.buildRobot(toBuild, dir, influence);
             } else {
                 break;
             }
         }
    }
}
