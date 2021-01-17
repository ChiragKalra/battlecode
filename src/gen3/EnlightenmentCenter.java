package gen3;

import battlecode.common.*;
import gen3.util.Pair;
import gen3.util.SpawnType;

import static gen3.RobotPlayer.*;
import static gen3.helpers.AttackHelper.checkForAttackCoordinates;
import static gen3.helpers.SpawnHelper.*;
import static gen3.util.SpawnType.getOptimalType;


public strictfp class EnlightenmentCenter {

    public static double RATIO_BID = 0.08;
    public static double RATIO_UNITS = 0.03;
    public static double RATIO_SPAWN_BUFF = 0.8;

    private static int mCooldown = 0;

    public static Pair<MapLocation, Integer> targetEC;

    private static boolean spawnOptimal() throws GameActionException {
        boolean spawned = false;
        if (targetEC !=null && targetEC.value >= 0 && targetEC.value <= rc.getInfluence()*RATIO_SPAWN_BUFF) {
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
            if (spawned) {
                mCooldown += got.cooldown;
            }
        } else  {
            mCooldown += SpawnType.AttackPolitician.cooldown;
        }

        return spawned;
    }


    public static void init() { }

    public static void move() throws GameActionException {
        if (mCooldown > 0) {
            mCooldown--;
        }

        if (rc.isReady() && mCooldown < 1) {
            spawnOptimal();
        }

        int bet = (int) (rc.getInfluence()*RATIO_BID);
        if (rc.canBid(bet) && rc.getTeamVotes() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            rc.bid(bet);
        } else if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            RATIO_UNITS += RATIO_BID;
        }

        Pair<MapLocation, Integer> got = checkForAttackCoordinates();
        if (got != null) {
            targetEC = got;
        }
    }
}
