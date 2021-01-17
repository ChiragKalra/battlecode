package gen3;

import battlecode.common.*;
import gen3.flags.EnlightenmentCenterFlag;
import gen3.flags.MuckrakerFlag;
import gen3.util.SpawnType;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.flags.MuckrakerFlag.*;
import static gen2.helpers.SpawnHelper.*;
import static gen2.util.SpawnType.getOptimalType;


public strictfp class EnlightenmentCenter {

    public static double RATIO_BID = 0.6;
    public static double RATIO_UNITS = 0.2;
    public static double RATIO_SPAWN_BUFF = 0.8;

    public static int xpDelta= 0;
    public static int[] xpAtRound = new int[1501];

    private static int mCooldown = 0;

    public final static HashMap<MapLocation, Integer> detectedECs = new HashMap<>();
    public static ArrayList<Integer>
            wanderingMuckrakers = new ArrayList<>(),
            placedMuckrakers = new ArrayList<>(),
            scannedMuckrakers = new ArrayList<>();

    public static MapLocation targetEC;
    public static int attackPoliticianCount = 0;


    public static void scanMuckrakersForCapturedECs () throws GameActionException {
        for (int i = placedMuckrakers.size()-1; i>=0; i--) {
            int id = placedMuckrakers.get(i);
            if (rc.canGetFlag(id)) {
                int flag = rc.getFlag(id);
                if (!isPlaced(flag)) {
                    if (isBroadcastingCaptured(flag)) {
                        MapLocation got = getCoordinatesFromFlag(flag);
                        detectedECs.remove(got);
                        if (targetEC.equals(got)) {
                            if (detectedECs.isEmpty()) {
                                targetEC = null;
                                attackPoliticianCount = 0;
                            } else {
                                targetEC = Collections.min(
                                        detectedECs.entrySet(),
                                        Comparator.comparingInt(Map.Entry::getValue)
                                ).getKey();
                                EnlightenmentCenterFlag.broadcastAttackCoordinates(targetEC);
                                attackPoliticianCount = 2;
                            }
                        }
                    }
                }
            } else {
                placedMuckrakers.remove(i);
            }
            if (rc.getRoundNum() > roundNumber) {
                return;
            }
        }
    }


    public static void scanMuckrakerFlagsForECs () throws GameActionException {
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                int flag = rc.getFlag(ri.getID());
                if (MuckrakerFlag.isBroadcastingEnemyECForTeam(flag)) {
                    detectedECs.put(
                            getCoordinatesFromFlag(flag),
                            1
                    );
                }
            }
        }

        for (int i = wanderingMuckrakers.size()-1; i>=0; i--) {
            int id = wanderingMuckrakers.get(i);
            wanderingMuckrakers.remove(i);
            if (rc.canGetFlag(id)) {
                int flag = rc.getFlag(id);
                if (!isPlaced(flag)) {
                    if (isBroadcastingNeutralEC(flag)) {
                        MapLocation coordinates = getCoordinatesFromFlag(flag);
                        if (!coordinates.equals(rc.getLocation())) {
                            detectedECs.put(
                                    coordinates,
                                    getNeutralHpFromFlag(flag)
                            );
                        }
                    } else if (isBroadcastingEnemyEC(flag)) {
                        MapLocation coordinates = getCoordinatesFromFlag(flag);
                        if (!coordinates.equals(rc.getLocation())) {
                            detectedECs.put(
                                    getCoordinatesFromFlag(flag),
                                    getEnemyHpFromFlag(flag)
                            );
                        }
                    }
                    scannedMuckrakers.add(id);
                } else {
                    placedMuckrakers.add(id);
                }
            }
            if (rc.getRoundNum() > roundNumber) {
                return;
            }
        }

        if (wanderingMuckrakers.isEmpty()) {
            wanderingMuckrakers = scannedMuckrakers;
            scannedMuckrakers = new ArrayList<>();
        }

        if ((targetEC==null || detectedECs.get(targetEC) <= 0) && !detectedECs.isEmpty()) {
            // choose to attack the lowest HP EC
            targetEC = Collections.min(detectedECs.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
            EnlightenmentCenterFlag.broadcastAttackCoordinates(targetEC);
            attackPoliticianCount = 2;
        }
    }

    private static boolean spawnOptimal() throws GameActionException {
        boolean spawned = false;
        if (attackPoliticianCount > 0) {
            int yeah = detectedECs.getOrDefault(targetEC, 0);
            if (targetEC != null && yeah > 0 && yeah <= rc.getInfluence()*RATIO_SPAWN_BUFF) {
                spawned = spawnAttackPolitician(targetEC, yeah);
            }
            if (spawned) {
                attackPoliticianCount--;
            }
        }
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

        return spawned;
    }


    public static void init() {
        xpAtRound[rc.getRoundNum()-1] = rc.getInfluence();
    }

    public static void move() throws GameActionException {
        xpDelta = Math.max(rc.getInfluence()-xpAtRound[rc.getRoundNum()-1], 0);
        xpAtRound[rc.getRoundNum()] = rc.getInfluence();

        if (mCooldown > 0) {
            mCooldown--;
        }

        if (rc.isReady() && mCooldown < 1) {
            spawnOptimal();
        }

        int bet = (int) (xpDelta*RATIO_BID);
        if (rc.canBid(bet) && rc.getTeamVotes() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            rc.bid(bet);
        } else if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            RATIO_UNITS += RATIO_BID;
        }

        if (rc.getRoundNum() > 150) {
            scanMuckrakersForCapturedECs();
        }
        if (rc.getRoundNum() > 50) {
            scanMuckrakerFlagsForECs();
        }
    }
}
