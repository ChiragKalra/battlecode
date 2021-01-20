package gen4.helpers;

import battlecode.common.*;

import gen4.util.PassabilityGrid;
import gen4.util.SpawnType;

import static gen4.EnlightenmentCenter.*;
import static gen4.RobotPlayer.*;
import static gen4.helpers.GridHelper.getDirectionFromAdjacentFlags;
import static gen4.helpers.MovementHelper.*;
import static gen4.util.Functions.sigmoid;

public class SpawnHelper {

    private static final int LIMIT_WALL_RADIUS = 21;

    private static int slandererHPFloor (int hp) {
        double func = (0.02 + 0.03*Math.exp(-0.001*hp))*hp;
        return (int) Math.ceil(Math.floor(func)/func*hp);
    }



    // layerQuantity = { 0, 8, 12, 16, 20, 28, 32, 40, 44, 48, 56, 60, 68, 72, 76, 84, };
    // private static final int[] sectorQuantity = { 0, 8, 20, 36, 56, 84, 116, 156, 200, 248, 304, 364, 432, 504, 580, 664, };
    private static final int[] roundExpanded = new int[LIMIT_WALL_RADIUS+1];
    private static int blockedRounds = 0;
    public static boolean shouldIncrementWallRadius() {
        double blockedFactor = 1-ratioDirectionsBlocked;
        if (rc.senseNearbyRobots(10).length > 18*blockedFactor) {
            blockedRounds++;
        } else {
            blockedRounds = 0;
        }
        boolean ans = blockedRounds >= 10 && currentRadius<LIMIT_WALL_RADIUS &&
                roundNumber-roundExpanded[currentRadius-1] > 9*currentRadius*blockedFactor ;
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

    private static int spawnDirectionSlan = 0;
    public static boolean spawnSlanderer() throws GameActionException {
        Direction dir = getOptimalDirection(directions[(spawnDirectionSlan++)%8]);
        if (dir == null || rc.senseNearbyRobots(sensorRadius, enemyTeam).length>0) {
            return false;
        }
        double earlyGameFactor = sigmoid((roundNumber-roundCaptured-800)/150.0);
        int xp = slandererHPFloor((int)(rc.getInfluence()*0.8)),
            minXp = slandererHPFloor(SpawnType.Slanderer.minHp),
            maxXp = (int) (slandererHPFloor(SpawnType.Slanderer.maxHp)*earlyGameFactor);
        xp = Math.max(minXp, xp);
        xp = Math.min(maxXp, xp);


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
        Direction dir = getOptimalDirection(directions[(spawnDirectionDefPol++)%8]);
        if (dir == null ) {
            return false;
        }
        int xp = (int)(rc.getInfluence()*0.005);
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
        PassabilityGrid grid = new PassabilityGrid(current, sensorRadius);
        double[] filter = DIRECTION_FACTOR,
                identity = {1, 1, 1, 1, 1};

        int dirInd = directionList.indexOf(to), decided = -1;

        if (to == null || to == Direction.CENTER) {
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
