package gen2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen2.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

import static gen2.RobotPlayer.rc;
import static gen2.flags.MuckrakerFlag.*;
import static gen2.helpers.SpawnHelper.*;


public strictfp class EnlightenmentCenter {
    private static final float RATIO_BET = 0f;
    public static final int FACTOR_MUCKRAKER_HP = 1;
    public static final int FACTOR_SLANDERER_HP = 2;
    public static final int FACTOR_POLITICIAN_HP = 1;

    public final static HashSet<Pair<MapLocation, Integer>>
            // store detected neutral centers
            neutralECs = new HashSet<>(),
            // move if pols sent to capture, remove if captured
            neutralPolsSent = new HashSet<>(),
            // store detected neutral centers
            enemyECs = new HashSet<>();


    public static void scanMuckrakerFlagsForVacancies() {
    }

    public static void scanMuckrakerFlagsForECs () {
        ArrayList<Integer> dead = new ArrayList<>();
        for (int id : wanderingMuckrakers) {
            try {
                int flag = rc.getFlag(id);
                if (!isPlaced(flag)) {
                    if (isBroadcastingNeutralEC(flag)) {
                        Pair<MapLocation, Integer> pair = new Pair<>(
                                getCoordinatesFromFlag(flag), getNeutralHpFromFlag(flag)
                        );
                        if (!neutralPolsSent.contains(pair)) {
                            neutralECs.add(pair);
                        }
                    }
                    if (isBroadcastingEnemyEC(flag)) {
                        MapLocation ml = getCoordinatesFromFlag(flag);
                        int hp = getEnemyHpFromFlag(flag);
                        enemyECs.add(new Pair<>(ml, hp));
                    }
                } else {
                    placedMuckrakers.add(id);
                }
            } catch (GameActionException e) {
                // muckraker has been martyred
                dead.add(id);
            }
        }
        // remove placed mucks
        for (int id : placedMuckrakers) {
            wanderingMuckrakers.remove(id);
        }
        // remove dead mucks
        for (int id : dead) {
            wanderingMuckrakers.remove(id);
        }
    }



    private static void spawnOptimal() throws GameActionException {
        boolean spawned = false;
        if (!neutralECs.isEmpty()) {
            for (Pair<MapLocation, Integer> got: neutralECs) {
                if (spawnAttackPolitician(got.key, got.value + 50)) {
                    neutralPolsSent.add(got);
                    neutralECs.remove(got);
                    spawned = true;
                    break;
                }
            }
        }
        if (!spawned) {
            switch (getOptimalType()) {
                case MUCKRAKER:
                    spawnMuckraker();
                    break;
                case SLANDERER:
                    spawnSlanderer();
                    break;
                case POLITICIAN:
                    spawnDefencePolitician();
            }
        }
    }


    public static void move() throws GameActionException {
        scanMuckrakerFlagsForECs();

        if (rc.isReady()) {
            spawnOptimal();
        }

        int totalInfluence = rc.getInfluence();
        if (rc.getRoundNum() > 500 && rc.canBid((int) (totalInfluence*RATIO_BET)) && rc.getTeamVotes() <= 1500) {
            rc.bid((int) (totalInfluence*RATIO_BET));
        }
    }
}
