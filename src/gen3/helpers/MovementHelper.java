package gen3.helpers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen3.util.Functions;
import gen3.util.PassabilityGrid;

import java.util.*;

import static gen3.RobotPlayer.actionRadius;
import static gen3.RobotPlayer.rc;
import static gen3.util.Functions.convolveCircularly;

public class MovementHelper {

    // max acceptable crowding ratio in a direction
    public static final double RATIO_CROWDING = 0.33;
    public static final int RADIUS_CROWDING = actionRadius;

    // adjacent direction preference factor
    public static final double[] DIRECTION_FACTOR = {.5, .25, .125, .0625, 0.03125};

    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    public static final List<Direction> directionList = Arrays.asList(directions);

    public static Direction getRandomDirection() {
        return (Direction) Functions.getRandom(directions);
    }

    public static Direction vectorAddition(Direction ... dirs) {
        MapLocation yeah = new MapLocation(0,0);
        for (Direction d : dirs) {
            yeah = yeah.add(d);
        }
        return (new MapLocation(0,0)).directionTo(yeah);
    }

    public static Direction getAntiCrowdingDirection(MapLocation current) throws GameActionException {
        byte[] occupied = new byte[8], total = new byte[8];
        int rad = (int) Math.sqrt(RADIUS_CROWDING);
        PassabilityGrid grid = new PassabilityGrid(current, RADIUS_CROWDING);
        for (int x = -rad; x <= rad; x++) {
            int limY = (int) Math.sqrt(RADIUS_CROWDING - x*x);
            for (int y = -limY; y <= limY; y++) {
                MapLocation ml = new MapLocation(x+current.x, y+current.y);
                int dirInd = directionList.indexOf(current.directionTo(ml));
                //  don't evaluate x = y = 0 condition
                if (dirInd != -1) {
                    total[dirInd]++;
                    occupied[dirInd] += grid.isBlockedOrOutside(ml) ? 1 : 0;
                }
            }
        }

        double[] ratios = new double[8], filter = {0.05, .2, .5, .2, .05};
        for (int i = 0; i < 8; i++) {
            ratios[i] = occupied[i] / (float) total[i];
        }
        ratios = convolveCircularly(ratios, filter);

        int maxInd = -1;
        double maxRatio = 0;
        for (int i = 0; i < 8; i++) {
            if (ratios[i] > maxRatio) {
                maxRatio = ratios[i];
                maxInd = i;
            }
        }
        if (maxRatio <= RATIO_CROWDING) {
            return null;
        }
        return directions[(maxInd+4)%8];
    }

    public static boolean tryMove (Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else if (rc.getCooldownTurns() < 1) {
            int dirInt = directionList.indexOf(dir);
            // if blocked by another robot, find the next best direction
            for (int i = 1; i<5; i++) {
                if (Math.random() < 0.5) {
                    Direction got = directions[Math.floorMod(dirInt + i, 8)];
                    if (rc.canMove(got)) {
                        rc.move(got);
                        return true;
                    }
                } else {
                    Direction got = directions[Math.floorMod(dirInt - i, 8)];
                    if (rc.canMove(got)) {
                        rc.move(got);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
