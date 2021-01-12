package gen2;

import battlecode.common.*;

import gen2.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static gen2.RobotPlayer.*;
import static gen2.flags.MuckrakerFlag.*;
import static gen2.flags.EnlightenmentCenterFlag.*;
import static gen2.helpers.SpawnHelper.*;
import static gen2.helpers.GridHelper.*;


public strictfp class EnlightenmentCenter {
    private static final float RATIO_BET = 0f;
    private static final int FACTOR_MUCKRAKER_HP = 1;


    public static HashSet<Pair<MapLocation, Integer>>
            // store detected neutral centers
            neutralECs = new HashSet<>(),
            // move if pols sent to capture, remove if captured
            neutralPolsSent = new HashSet<>(),
            // store detected neutral centers
            enemyECs = new HashSet<>();


    public static void scanMuckrakerFlagsForECs () throws GameActionException {
        for (int id : muckrakersBuilt) {
            int flag = rc.getFlag(id);
            if (isPlaced(flag)) {
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
            }
        }
    }

    public static HashSet<Integer> muckrakersBuilt = new HashSet<>();
    private static boolean spawnMuckraker() throws GameActionException {
        Direction dir = getOptimalDirection(getDirectionFromAdjacentFlags(rc.getLocation()));
        double cooldown = rc.sensePassability(rc.getLocation());
        int xp = (int) (FACTOR_MUCKRAKER_HP/cooldown);
        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            muckrakersBuilt.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }

    public static HashMap<MapLocation, Integer>attackPoliticiansBuilt = new HashMap<>();
    private static boolean spawnAttackPolitician (MapLocation toAttack, int hp) throws GameActionException {
        Direction dir = getOptimalDirection(rc.getLocation().directionTo(toAttack));
        int xp = hp + 11;
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            broadcastAttackCoordinates(toAttack);
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            attackPoliticiansBuilt.put(toAttack, rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }

    private static void spawnSlanderer() {}

    private static void spawnOptimal() throws GameActionException {
        boolean spawned = false;
        if (!neutralECs.isEmpty()) {
            for (Pair<MapLocation, Integer> got: neutralECs) {
                if (spawnAttackPolitician(got.key, got.value)) {
                    neutralPolsSent.add(got);
                    neutralECs.remove(got);
                    spawned = true;
                    break;
                }
            }
        }
        if (!spawned && round < 500) {
            spawnMuckraker();
        }
    }


    public static void move() throws GameActionException {
        if (DEBUG) {
            if (round % 500 == 0) {
                System.out.println("Round number:- " + round);
            }
        }

        scanMuckrakerFlagsForECs();

        if (rc.isReady()) {
            spawnOptimal();
        }

        int totalInfluence = rc.getInfluence();
        if (round > 500 && rc.canBid((int) (totalInfluence*RATIO_BET)) && rc.getTeamVotes() <= 1500) {
            rc.bid((int) (totalInfluence*RATIO_BET));
        }
    }
}
