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
        RobotType.POLITICIAN,
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
        RobotType.MUCKRAKER,
        RobotType.MUCKRAKER,
    };

    static int lastDirectionInd = 7;
    static int muckrakersBuilt = 0;

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
    private static Direction getOptimalDirection() {
        lastDirectionInd += 2;
        if (lastDirectionInd >= directions.length) {
            lastDirectionInd = (lastDirectionInd+1)%2;
        }
        return directions[lastDirectionInd];
    }

    public static void move() throws GameActionException {
        if (DEBUG) {
            if (round % 500 == 0) {
                System.out.println("Round number:- " + round);
            }
        }

        RobotType toBuild = randomSpawnableRobotType();
        float influenceFac = 0, multiplier = mType.actionCooldown / (float) rc.sensePassability(rc.getLocation());
        switch (toBuild) {
            case MUCKRAKER:
                influenceFac = 1f;
                break;
            case POLITICIAN:
                influenceFac = 3f;
                break;
            case SLANDERER:
                influenceFac = 1.5f;
        }

        Direction dir = getOptimalDirection();

        if (rc.canBuildRobot(toBuild, dir, (int) (multiplier*influenceFac))) {
            rc.buildRobot(toBuild, dir, (int) (multiplier*influenceFac));
        }

        int totalInfluence = rc.getInfluence();
        if (rc.canBid(totalInfluence/2) && rc.getTeamVotes() <= 1500) {
            rc.bid(totalInfluence/2);
        }
    }


    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {

    }
}
