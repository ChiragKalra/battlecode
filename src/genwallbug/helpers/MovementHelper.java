package gen5.helpers;

import battlecode.common.*;
import gen5.util.Functions;
import java.util.*;

import static gen5.RobotPlayer.*;
import static gen5.util.Functions.convolveCircularly;

public class MovementHelper {

    // max acceptable crowding ratio in a direction
    public static final double RATIO_CROWDING = 0.33;

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

    public static boolean goTo (MapLocation dest) throws GameActionException {
        MapLocation now = rc.getLocation();
        return tryMove(now.directionTo(dest), now.isAdjacentTo(dest));
    }


    public static boolean tryMove (Direction dir, boolean force) throws GameActionException {
        if (!force) {
            MapLocation ml = rc.getLocation();
            double left = rc.canMove(dir.rotateLeft()) ? rc.sensePassability(ml.add(dir.rotateLeft())) : 0,
                    straight = rc.canMove(dir) ? rc.sensePassability(ml.add(dir)) : 0,
                    right = rc.canMove(dir.rotateRight()) ? rc.sensePassability(ml.add(dir.rotateRight())) : 0;

            if (straight > 0 && straight >= right && straight >= left) {
                rc.move(dir);
                return true;
            } else if (left > 0 && left >= right && straight <= left) {
                rc.move(dir.rotateLeft());
                return true;
            } else if (right > 0 && straight <= right && right >= left) {
                rc.move(dir.rotateRight());
                return true;
            }
        }

        if (rc.getCooldownTurns() < 1) {
            int dirInt = directionList.indexOf(dir);
            // if blocked by another robot, find the next best direction
            for (int i = force ? 0 : 2; i<5; i++) {
                Direction got = directions[Math.floorMod(dirInt + i, 8)];
                if (rc.canMove(got)) {
                    rc.move(got);
                    return true;
                }
                got = directions[Math.floorMod(dirInt - i, 8)];
                if (rc.canMove(got)) {
                    rc.move(got);
                    return true;
                }
            }
        }
        return false;
    }


    public static MapLocation getOptimalLocationToEmpower () throws GameActionException {
        MapLocation ec = null;
        RobotInfo[] nearby = rc.senseNearbyRobots(9);
        for (RobotInfo ri: nearby) {
            if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                ec = ri.location;
                break;
            }
        }
        if (ec == null) {
            return null;
        }

        MapLocation current = rc.getLocation();

        for (int i = 0; i < 8; i+=2) {
            MapLocation potential = ec.add(directions[i]);
            if (rc.onTheMap(potential) && !rc.isLocationOccupied(potential)) {
                boolean is = true;
                for (int j = 0; j < 8; j+=2) {
                    if (Math.abs(i-j) == 4) {
                        continue;
                    }
                    MapLocation check = potential.add(directions[i]);
                    if (!current.equals(check) && rc.onTheMap(check) && check.x % 5 == 0 && check.y % 5 == 0) {
                        is = false;
                        break;
                    }
                }
                if (is) {
                    return potential;
                }
            }
        }
        return null;
    }


    public static Direction getAntiCrowdingDirection(MapLocation current) {
        byte[] occupied = new byte[8];
        int total = 0;
        for (MapLocation ml : rc.detectNearbyRobots(actionRadius)) {
            occupied[directionList.indexOf(current.directionTo(ml))]++;
            total++;
        }


        double[] ratios = new double[8], filter = {.2, .6, .2};
        for (int i = 0; i < 8; i++) {
            ratios[i] = occupied[i] / (float) total;
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

/*

    private static MapLocation[] relativeLocations;
    private static int cachedRadius = 0, ptr = 0;

    public static MapLocation[] getCircumferencePoints (MapLocation center, int radiusSquared) {
        int rad = (int) Math.sqrt(radiusSquared);
        if (relativeLocations == null || cachedRadius != radiusSquared) {
            relativeLocations = new MapLocation[300];
            ptr = 0;
            for (int x = 1; x <= rad; x++) {
                int limY = (int) Math.sqrt(radiusSquared - x*x);
                relativeLocations[ptr++] = new MapLocation(x, limY);
                relativeLocations[ptr++] = new MapLocation(x, -limY);
                relativeLocations[ptr++] = new MapLocation(-x, limY);
                relativeLocations[ptr++] = new MapLocation(-x, -limY);

                int c = radiusSquared - (x*x + 2*x + 1);
                for (int y = 1-limY; y*y > c && y < 0; y++) {
                    relativeLocations[ptr++] = new MapLocation(x, y);
                    relativeLocations[ptr++] = new MapLocation(x, -y);
                    relativeLocations[ptr++] = new MapLocation(-x, y);
                    relativeLocations[ptr++] = new MapLocation(-x, -y);
                }
            }
            relativeLocations[ptr++] = new MapLocation(rad, 0);
            relativeLocations[ptr++] = new MapLocation(-rad, 0);
            relativeLocations[ptr++] = new MapLocation(0, rad);
            relativeLocations[ptr++] = new MapLocation(0, -rad);
            cachedRadius = radiusSquared;
        }
        MapLocation[] ret = new MapLocation[ptr];
        for (int i = 0; i < ptr; i++) {
            ret[i] = new MapLocation(center.x + relativeLocations[i].x, center.y + relativeLocations[i].y);
        }
        return ret;
    }
*/

}
