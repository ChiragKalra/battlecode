package gen4;

import battlecode.common.*;
import gen4.util.Pair;
import gen4.util.SpawnType;

import static gen4.RobotPlayer.*;
import static gen4.helpers.AttackHelper.checkForAttackCoordinates;
import static gen4.helpers.SpawnHelper.*;
import static gen4.util.SpawnType.getOptimalType;


public strictfp class EnlightenmentCenter {

    public static int currentRadius = 3;

    public static double RATIO_BID = 0.08;
    public static double RATIO_UNITS = 0.02;
    public static double RATIO_SPAWN_BUFF = 0;

    public static int roundCaptured = 1;
    public static Pair<MapLocation, Integer> targetEC;


    private static boolean spawnOptimal() throws GameActionException {
        boolean spawned = false;
        if (targetEC !=null && targetEC.value <= rc.getInfluence()*RATIO_SPAWN_BUFF) {
            spawned = spawnAttackPolitician(targetEC.key, targetEC.value);
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

        if (rc.isReady()) {
            spawnOptimal();
        }

        int bet = (int) (rc.getInfluence() * RATIO_BID * Math.pow(1.05, rc.getInfluence()/1000.0));
        if (rc.getRoundNum() >= 250 && rc.canBid(bet) && rc.getTeamVotes() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            rc.bid(bet);
        } else if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            RATIO_UNITS += RATIO_BID;
        }

        Pair<MapLocation, Integer> got = checkForAttackCoordinates();
        if (got != null) {
            if (got.value >= 0) {
                targetEC = got;
            }
        }
    }
}
