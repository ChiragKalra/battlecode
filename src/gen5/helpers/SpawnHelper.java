package gen5.helpers;

import battlecode.common.*;

import gen5.util.SpawnType;

import static gen5.EnlightenmentCenter.*;
import static gen5.RobotPlayer.*;
import static gen5.helpers.GridHelper.getDirectionFromAdjacentFlags;
import static gen5.helpers.MovementHelper.*;
import static gen5.util.Functions.sigmoid;

public class SpawnHelper {

    private static final int LIMIT_WALL_RADIUS = 50;

    public static int slandererHPFloor (int hp) {
        double func = (0.02 + 0.03*Math.exp(-0.001*hp))*hp;
        return (int) Math.ceil(Math.floor(func)/func*hp);
    }


    // layerQuantity = { 0, 8, 12, 16, 20, 28, 32, 40, 44, 48, 56, 60, 68, 72, 76, 84, };
    // private static final int[] sectorQuantity = { 0, 8, 20, 36, 56, 84, 116, 156, 200, 248, 304, 364, 432, 504, 580, 664, };
    private static final int[] roundExpanded = new int[LIMIT_WALL_RADIUS+1];
    private static int blockedRounds = 0;
    public static boolean shouldIncrementWallRadius() {
        int capacity  = 20;
        switch (directionsBlocked) {
            case 2:
                capacity = 12;
                break;
            case 3:
                capacity = 7;
        }
        if (rc.senseNearbyRobots(10).length >= capacity-2) {
            blockedRounds++;
        } else {
            blockedRounds = 0;
        }
        boolean ans = blockedRounds >= 10 && currentRadius<LIMIT_WALL_RADIUS &&
                roundNumber-roundExpanded[currentRadius-1] > 8*currentRadius*capacity/20 ;
        if (ans) {
            roundExpanded[currentRadius+1] = roundNumber;
            blockedRounds = 0;
        }
        return ans;
    }

    private static int spawnDirectionMuck = 0;
    public static boolean spawnMuckraker() throws GameActionException {
        Direction got = getDirectionFromAdjacentFlags(rc.getLocation()),
                dir = getOptimalDirection(got != null ? got : directions[(spawnDirectionMuck++)%8]);
        if (dir == null ) {
            return false;
        }

        int xp = (int)(rc.getInfluence()*RATIO_UNITS/4);
        xp = Math.max(SpawnType.Muckraker.minHp, xp);
        xp = Math.min(SpawnType.Muckraker.maxHp, xp);

        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            return true;
        }
        return false;
    }

    public static boolean spawnAttackPolitician (MapLocation toAttack, int hp) throws GameActionException {
        Direction dir = getOptimalDirection(rc.getLocation().directionTo(toAttack));
        if (dir == null) {
            return false;
        }
        int xp = Math.max(hp + 11, (int)(rc.getInfluence()*RATIO_UNITS));
        if (xp % 2 == 0) {
            xp++;
        }
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            return true;
        }
        return false;
    }

    private static int spawnDirectionSlan = 1;
    public static boolean spawnSlanderer() throws GameActionException {
        Direction dir = getOptimalDirection(directions[(spawnDirectionSlan+=2)%8]);
        if (dir == null || rc.senseNearbyRobots(sensorRadius, enemyTeam).length>0) {
            return false;
        }
        double earlyGameFactor = sigmoid((roundNumber-roundCaptured-800)/150.0);
        int xp = slandererHPFloor((int)(rc.getInfluence()*0.3)),
            minXp = slandererHPFloor(SpawnType.Slanderer.minHp),
            maxXp = (int) (slandererHPFloor(SpawnType.Slanderer.maxHp)*earlyGameFactor);
        xp = Math.max(minXp, xp);
        xp = Math.min(maxXp == 0 ? minXp : maxXp, xp);


        if (rc.canBuildRobot(RobotType.SLANDERER, dir, xp)) {
            rc.buildRobot(RobotType.SLANDERER, dir, xp);
            return true;
        } else if (rc.canBuildRobot(RobotType.SLANDERER, dir, minXp)) {
            rc.buildRobot(RobotType.SLANDERER, dir, minXp);
            return true;
        }
        return false;
    }


    private static int spawnDirectionDefPol = 0;
    public static boolean spawnDefencePolitician() throws GameActionException {
        Direction dir = getOptimalDirection(directions[(spawnDirectionDefPol+=2)%8]);
        if (dir == null ) {
            return false;
        }
        int xp = (int)(rc.getInfluence()*0.03);
        xp = Math.max(SpawnType.DefensePolitician.minHp, xp);
        xp = Math.min(SpawnType.DefensePolitician.maxHp, xp);
        if (xp % 2 == 1) {
            xp++;
        }
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            return true;
        }
        return false;
    }


    public static Direction getOptimalDirection (Direction to) throws GameActionException {
        MapLocation current = rc.getLocation();
        if (to == null) {
            to = Direction.SOUTHEAST;
        }
        int dirInt = directionList.indexOf(to);
        // if blocked by another robot, find the next best direction
        for (int i = 0; i < 5; i++) {
            Direction got = directions[Math.floorMod(dirInt + i, 8)];
            MapLocation ml = current.add(got);
            if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                return got;
            }
            got = directions[Math.floorMod(dirInt - i, 8)];
            ml = current.add(got);
            if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                return got;
            }
        }
        return null;
    }

}
