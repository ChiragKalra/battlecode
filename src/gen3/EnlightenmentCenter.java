package gen3;

import battlecode.common.*;
import gen3.util.Pair;
import gen3.util.SpawnType;

import static gen3.RobotPlayer.*;
import static gen3.helpers.AttackHelper.checkForAttackCoordinates;
import static gen3.helpers.SpawnHelper.*;
import static gen3.util.SpawnType.getOptimalType;


public strictfp class EnlightenmentCenter {

    public static int currentRadius = 3;

    public static double RATIO_BID = 0.1;
    public static double RATIO_UNITS = 0.03;
    public static double RATIO_SPAWN_BUFF = 0.8;


    private static int roundCaptured = 1;
    public static Pair<MapLocation, Integer> targetEC;


    private static boolean spawnOptimal() throws GameActionException {
        boolean spawned = false;
        if (targetEC !=null && targetEC.value <= rc.getInfluence()*RATIO_SPAWN_BUFF) {
            spawned = spawnAttackPolitician(targetEC.key, targetEC.value);
        }
        if (roundNumber-roundCaptured<30) {
            spawnDefencePolitician();
        }
        if (!spawned) {
            SpawnType got = getOptimalType();
            switch (got) {
                case Muckraker:
                    spawned = spawnMuckraker();
                    break;
                case DefensePolitician:
                    spawned = spawnDefencePolitician();
                    break;
                case Slanderer:
                    spawned = spawnSlanderer();
                    break;
            }
        }

        return spawned;
    }


    public static void init() {
        roundCaptured = rc.getRoundNum();
    }

    public static void move() throws GameActionException {

        /*int[][] colors = {
                {0, 0, 255},
                {255, 255, 255},
                {0, 255, 255},
                {255, 0, 255},
                {255, 255, 0},
                {255, 0, 0},
                {125, 0, 125},
        };

        //indicator dots near EC to show point on the circumference of radius squared

        if (roundNumber == 1) {
            Logger logger = new Logger("circim", false);
            for (int i = 1; i <= 7; i++) {
                MovementHelper.getCircumferencePoints(rc.getLocation(), i * i + 1);
            }
        }*/


        if (rc.isReady()) {
            spawnOptimal();
        }

        int bet = (int) (rc.getInfluence()*RATIO_BID);
        if (rc.canBid(bet) && rc.getTeamVotes() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            rc.bid(bet);
        } else if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            RATIO_UNITS += RATIO_BID;
        }

        Pair<MapLocation, Integer> got = checkForAttackCoordinates();
        if (got != null && got.value >= 0) {
            targetEC = got;
        }
    }
}
