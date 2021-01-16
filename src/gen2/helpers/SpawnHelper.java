package gen2.helpers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import gen2.util.PassabilityGrid;
import gen2.util.SpawnType;

import java.util.ArrayList;
import java.util.HashMap;

import static gen2.EnlightenmentCenter.*;
import static gen2.RobotPlayer.rc;
import static gen2.RobotPlayer.sensorRadius;
import static gen2.flags.EnlightenmentCenterFlag.broadcastAttackCoordinates;
import static gen2.helpers.GridHelper.getDirectionFromAdjacentFlags;
import static gen2.helpers.MovementHelper.*;

public class SpawnHelper {

    private static int slandererHPFloor (int hp) {
        double func = (0.02 + 0.03*Math.exp(-0.001*hp))*hp;
        return (int) Math.ceil(Math.floor(func)/func*hp);
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
        int xp = SpawnType.Muckraker.minHp;
        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            wanderingMuckrakers.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }

    public static final HashMap<MapLocation, Integer> attackPoliticiansBuilt = new HashMap<>();
    public static boolean spawnAttackPolitician (MapLocation toAttack) throws GameActionException {
        Direction dir = getOptimalDirection(rc.getLocation().directionTo(toAttack));
        if (dir == null) {
            return false;
        }
        int hp = slandererHPFloor((int)(xpDelta*RATIO_UNITS)), xp = hp + 11;
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            if (toAttack != null) {
                detectedECs.put(toAttack, detectedECs.get(toAttack)-hp);
                broadcastAttackCoordinates(toAttack);
            }
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
