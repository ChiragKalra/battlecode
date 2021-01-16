package gen2.helpers;

import battlecode.common.*;
import gen2.util.PassabilityGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static gen2.EnlightenmentCenter.*;
import static gen2.flags.EnlightenmentCenterFlag.broadcastAttackCoordinates;
import static gen2.helpers.GridHelper.getDirectionFromAdjacentFlags;
import static gen2.helpers.MovementHelper.*;
import static gen2.RobotPlayer.*;
import static gen2.util.Functions.sigmoid;

public class SpawnHelper {

    private static int slandererHPFloor (int hp) {
        double func = (0.02 + 0.03*Math.exp(-0.001*hp))*hp;
        return (int) Math.ceil(Math.floor(func)/func*hp);
    }

    private static double getMuckrakerProbability (int round) {
        return 1 - 0.8*sigmoid((round-225)/35.0);
    }

    private static double getPoliticianProbability (int round) {
        return sigmoid((round-300)/45.0);
    }

    private static double getSlandererProbability (int round) {
        return sigmoid((round-400)/60.0);
    }

    public static RobotType getOptimalType() {
        int round = rc.getRoundNum();
        double mr = getMuckrakerProbability(round),
                pol = getPoliticianProbability(round),
                slan = getSlandererProbability(round),
                total = mr + pol + slan,
                rand = Math.random();
        mr /= total;
        pol /= total;
        if (rand < mr) {
            return RobotType.MUCKRAKER;
        } else if (rand < mr + pol) {
            return RobotType.POLITICIAN;
        } else {
            return RobotType.SLANDERER;
        }
    }

    public static ArrayList<Integer>
            wanderingMuckrakers = new ArrayList<>(),
            scannedMuckrakers = new ArrayList<>();
    public static final ArrayList<Integer> placedMuckrakers = new ArrayList<>();
    public static boolean spawnMuckraker() throws GameActionException {
        Direction dir = getOptimalDirection(getDirectionFromAdjacentFlags(rc.getLocation()));
        if (dir == null ) {
            return false;
        }
        int xp = FACTOR_MUCKRAKER_HP;
        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            wanderingMuckrakers.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }

    public static final HashMap<MapLocation, Integer> attackPoliticiansBuilt = new HashMap<>();
    public static boolean spawnAttackPolitician (MapLocation toAttack, int hp) throws GameActionException {
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

    public static boolean spawnSlanderer() throws GameActionException {
        Direction dir = getOptimalDirection(null);
        if (dir == null ) {
            return false;
        }
        //int xp = slandererHPFloor(Math.max(25, (int)(xpDelta*RATIO_UNITS)));
        int xp = slandererHPFloor(Math.max(25, 2));
        if (rc.canBuildRobot(RobotType.SLANDERER, dir, xp)) {
            rc.buildRobot(RobotType.SLANDERER, dir, xp);
            wanderingMuckrakers.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }

    public static boolean spawnDefencePolitician() throws GameActionException {
        Direction dir = getOptimalDirection(null);
        if (dir == null ) {
            return false;
        }
        //int xp = slandererHPFloor(Math.max((int) (xpDelta * RATIO_UNITS), 25));
        int xp = slandererHPFloor(Math.max(25, 2));
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            wanderingMuckrakers.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }


    public static Direction getOptimalDirection (Direction to) throws GameActionException {
        MapLocation current = rc.getLocation();
        PassabilityGrid grid = new PassabilityGrid(current, sensorRadius);
        double[] filter = DIRECTION_FACTOR,
                identity = {1, 1, 1, 1, 1};

        int dirInd = directionList.indexOf(to), decided = -1;

        if (to == null) {
            filter = identity;
            dirInd = 0;
        }

        double factor = 0;

        for (int i = 0; i < 8; i++) {
            Direction d = directions[i];
            int filterInd = Math.min(Math.abs(dirInd-i), Math.abs(dirInd - 8 + i));
            double cur = filter[filterInd]*grid.getRelative(d.dx, d.dy);
            if (cur > factor) {
                decided = i;
                factor = cur;
            }
        }

        if (decided == -1) {
            return null;
        }

        return directions[decided];
    }

}
