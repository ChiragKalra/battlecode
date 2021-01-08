package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;


public strictfp class EnlightenmentCenter {
    static final RobotType[] spawnableRobot = {
        //RobotType.POLITICIAN,
        //RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static int lastDirectionInd = 0;

    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    static void move() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 1;

        Direction dir = directions[lastDirectionInd];

        if (rc.canBuildRobot(toBuild, dir, influence)) {
            rc.buildRobot(toBuild, dir, influence);

            lastDirectionInd += 2;
            if (lastDirectionInd >= directions.length) {
                lastDirectionInd = (lastDirectionInd+1)%2;
            }
        }
    }
}
