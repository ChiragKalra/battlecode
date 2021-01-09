package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.MovementHelper.*;

/*
 # Muckraker Flag

     0      - searching/placed
     1-4    - politician approaching direction
     5-7    - vacant muckraker spot direction
     8-14   - enemy/neutral enlightenment center x
     15-21  - enemy/neutral enlightenment center y
     22     - attack/gird muckraker
     23     - undecided

 */



public strictfp class EnlightenmentCenter {
    static final RobotType[] spawnableRobot = {
        //RobotType.POLITICIAN,
        //RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static int lastDirectionInd = 7;

    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }


    /*
     * Factors
     *      1) Pass-ability of adjacent square
     *      2) Cool down time of center
     *      3) Number of bots to be produced
     *      4) Rotation to free up squares
     */
    // TODO select direction based on adjacent muckraker directions and pass-ability
    static Direction getOptimalDirection() {
        lastDirectionInd += 2;
        if (lastDirectionInd >= directions.length) {
            lastDirectionInd = (lastDirectionInd+1)%2;
        }
        return directions[lastDirectionInd];
    }

    static void move() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 1;//(int) (2 * MAX_GENERATED_INFLUENCE / (GameConstants.GAME_MAX_NUMBER_OF_ROUNDS * rc.sensePassability(rc.getLocation())));

        Direction dir = getOptimalDirection();

        if (rc.canBuildRobot(toBuild, dir, influence)) {
            rc.buildRobot(toBuild, dir, influence);
        }
    }
}
