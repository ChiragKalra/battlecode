package gen2;

import battlecode.common.*;

import gen2.util.Pair;

import java.util.*;

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
        ArrayList<Integer> placed = new ArrayList<>();
        for (int id : wanderingMuckrakers) {
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
                placed.add(id);
            }
        }
        // remove placed integers from hashset for better speed
        for (int id : placed) {
            wanderingMuckrakers.remove(id);
        }
        placedMuckrakers += placed.size();
    }

    public static HashSet<Integer> wanderingMuckrakers = new HashSet<>();
    public static int placedMuckrakers = 0;
    private static boolean spawnMuckraker() throws GameActionException {
        Direction dir = getOptimalDirection(getDirectionFromAdjacentFlags(rc.getLocation()));
        if (dir == null ) {
            return false;
        }
        double cooldown = rc.sensePassability(rc.getLocation());
        int xp = (int) (FACTOR_MUCKRAKER_HP/cooldown);
        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            wanderingMuckrakers.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }

    public static HashMap<MapLocation, Integer>attackPoliticiansBuilt = new HashMap<>();
    private static boolean spawnAttackPolitician (MapLocation toAttack, int hp) throws GameActionException {
        Direction dir = getOptimalDirection(rc.getLocation().directionTo(toAttack));
        if (dir == null) {
            return false;
        }
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
                if (spawnAttackPolitician(got.key, got.value + 50)) {
                    neutralPolsSent.add(got);
                    neutralECs.remove(got);
                    spawned = true;
                    break;
                }
            }
        }
        if (!spawned && (wanderingMuckrakers.size()+placedMuckrakers) < 121 && round < 500) {
            spawnMuckraker();
        }
    }


    public static void move() throws GameActionException {
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
