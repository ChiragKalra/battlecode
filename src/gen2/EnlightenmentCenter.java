package gen2;

import battlecode.common.*;
import gen2.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

import static gen2.RobotPlayer.log;
import static gen2.RobotPlayer.rc;
import static gen2.flags.MuckrakerFlag.*;
import static gen2.helpers.SpawnHelper.*;


public strictfp class EnlightenmentCenter {
    public static final int FACTOR_MUCKRAKER_HP = 1;
    public static final int
            COOLDOWN_SLANDERER = 5,
            COOLDOWN_POLITICIAN = 4,
            COOLDOWN_MUCKRAKER = 1;

    public static final double RATIO_BID = 0.6;
    //public static final double RATIO_UNITS = 0.2;

    //public static int xpDelta= 0;
    //public static int[] xpAtRound = new int[1501];

    private static int mCooldown = 0;

    public final static HashSet<Pair<MapLocation, Integer>>
            // store detected neutral centers
            neutralECs = new HashSet<>(),
            // move if pols sent to capture, remove if captured
            neutralPolsSent = new HashSet<>(),
            // store detected neutral centers
            enemyECs = new HashSet<>();


    public static void scanMuckrakerFlagsForVacancies() {
    }

    public static void scanMuckrakerFlagsForECs () throws GameActionException {
        ArrayList<Integer> dead = new ArrayList<>();
        if (!scannedMuckrakers.isEmpty() && wanderingMuckrakers.isEmpty()) {
            wanderingMuckrakers = scannedMuckrakers;
            scannedMuckrakers = new HashSet<>();
        }

        for (int id : wanderingMuckrakers) {
            if (!rc.canGetFlag(id)) {
                // muckraker has been martyred
                dead.add(id);
            } else {
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
                    scannedMuckrakers.add(id);
                } else {
                    placedMuckrakers.add(id);
                }
            }

            if (Clock.getBytecodesLeft() < 2000) {
                break;
            }
        }
        // remove scanned mucks
        for (int id : scannedMuckrakers) {
            wanderingMuckrakers.remove(id);
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



    private static boolean spawnOptimal() throws GameActionException {
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
        if (spawned) {
            return true;
        }

        switch (getOptimalType()) {
            case MUCKRAKER:
                if (spawnMuckraker()) {
                    mCooldown += COOLDOWN_MUCKRAKER;
                    return true;
                }
            case SLANDERER:
                if (spawnSlanderer()) {
                    mCooldown += COOLDOWN_SLANDERER;
                    return true;
                }
            case POLITICIAN:
                if (spawnDefencePolitician()) {
                    mCooldown += COOLDOWN_POLITICIAN;
                    return true;
                }
        }

        return false;
    }


    public static void init() {
        //xpAtRound[rc.getRoundNum()-1] = rc.getInfluence();
    }

    public static void move() throws GameActionException {
        //xpDelta = rc.getInfluence()-xpAtRound[rc.getRoundNum()-1];
        //xpAtRound[rc.getRoundNum()] = rc.getInfluence();

        if (mCooldown > 0) {
            mCooldown--;
        }

        if (rc.isReady() && mCooldown < 1) {
            spawnOptimal();
        }

        int bet = (int) (rc.getInfluence()*0.2);
        if (rc.getRoundNum() > 250 && rc.canBid(bet) && rc.getTeamVotes() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            rc.bid(bet);
        }

        scanMuckrakerFlagsForECs();
    }
}
