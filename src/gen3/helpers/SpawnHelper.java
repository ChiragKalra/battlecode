package gen3.helpers;

import battlecode.common.*;

import gen3.util.PassabilityGrid;
import gen3.util.SpawnType;

import static gen3.EnlightenmentCenter.*;
import static gen3.RobotPlayer.*;
import static gen3.helpers.GridHelper.getDirectionFromAdjacentFlags;
import static gen3.helpers.MovementHelper.*;

public class SpawnHelper {

    private static int slandererHPFloor (int hp) {
        double func = (0.02 + 0.03*Math.exp(-0.001*hp))*hp;
        return (int) Math.ceil(Math.floor(func)/func*hp);
    }

    public static boolean spawnMuckraker() throws GameActionException {
        Direction dir = getOptimalDirection(getDirectionFromAdjacentFlags(rc.getLocation()));
        if (dir == null ) {
            return false;
        }

        int xp = (int)(rc.getInfluence()*RATIO_UNITS);
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
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            return true;
        }
        return false;
    }

    private static int ccvRotation = 1;
    public static boolean spawnSlanderer() throws GameActionException {
        Direction dir = getOptimalDirection(directions[ccvRotation]);
        ccvRotation += 2;
        if (ccvRotation > 7) {
            ccvRotation = 1;
        }
        if (dir == null ) {
            return false;
        }
        //int xp = slandererHPFloor(Math.max(25, (int)(xpDelta*RATIO_UNITS)));
        int xp = slandererHPFloor((int)(rc.getInfluence()*RATIO_UNITS));
        xp = Math.max(slandererHPFloor(SpawnType.Slanderer.minHp), xp);
        xp = Math.min(slandererHPFloor(SpawnType.Slanderer.maxHp), xp);


        if (rc.canBuildRobot(RobotType.SLANDERER, dir, xp)) {
            rc.buildRobot(RobotType.SLANDERER, dir, xp);
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
