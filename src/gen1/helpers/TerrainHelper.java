package gen1.helpers;

import battlecode.common.*;
import gen1.dataclasses.PassabilityGrid;

import java.util.*;

import static gen1.RobotPlayer.*;


public class TerrainHelper {
    private static int minX = 0, maxX = Integer.MAX_VALUE, minY = 0, maxY = Integer.MAX_VALUE;

    public static void markOutsideMap(MapLocation current, MapLocation got) throws GameActionException {
        MapLocation parX = new MapLocation(got.x, current.y), parY = new MapLocation(current.x, got.y);
        if (current.isWithinDistanceSquared(parX, sensorRadius) && !rc.onTheMap(parX)) {
            if (got.x < current.x) {
                minX = Math.max(got.x, minX);
            } else if (current.x < got.x) {
                maxX = Math.min(got.x, maxX);
            }
        }
        if (current.isWithinDistanceSquared(parY, sensorRadius) && !rc.onTheMap(parY)) {
            if (got.y < current.y) {
                minY = Math.max(got.y, minY);
            } else if (current.y < got.y) {
                maxY = Math.min(got.y, maxY);
            }
        }
    }

    public static boolean isOutsideMap (MapLocation ml) {
        return maxX <= ml.x || ml.x <= minX || maxY <= ml.y || ml.y <= minY;
    }


/*

     *
     * @return
     *      a grid with passability values around the given map location
     *
     *      passability[x][y] = 0, for
     *          - locations not on the map
     *          - locations having robots
     *          - locations outside of radius
     *
    public static PassabilityGrid getPassabilityGrid (MapLocation current) throws GameActionException {
        int limX = (int) Math.sqrt(sensorRadius), sz = 2*limX+1, bt = 0;
        double[] grid = new double[sz*sz];
        MapLocation ml;
        grid[sz*limX + limX] = rc.sensePassability(current);
        for (int x = -limX; x <= limX; x++) {
            int limY = (int) Math.sqrt(sensorRadius - x*x);
            for (int y = -limY; y <= limY; y++) {
                ml = new MapLocation(x+current.x, y+current.y);
                try {
                    if (!rc.isLocationOccupied(ml)) {
                        grid[sz*(x + limX) + (y + limX)] = rc.sensePassability(ml);
                    }
                } catch (GameActionException ignored) {}
            }
        }
        return new PassabilityGrid(current, sensorRadius, grid);
    }
 */
    private static ArrayList<MapLocation> relativeLocations;

    private static MapLocation[] getCircumferencePoints(MapLocation center) {
        int rad = (int) Math.sqrt(sensorRadius);
        if (relativeLocations == null) {
            relativeLocations = new ArrayList<>();
            for (int x = -rad; x <= rad; x++) {
                int limY = (int) Math.sqrt(sensorRadius - x*x);
                if (Math.abs(x) == rad) {
                    for (int y = -limY; y <= limY; y++) {
                        relativeLocations.add(new MapLocation(x, y));
                    }
                } else {
                    relativeLocations.add(new MapLocation(x, limY));
                    relativeLocations.add(new MapLocation(x, -limY));
                }
            }
        }
        MapLocation[] ret = new MapLocation[relativeLocations.size()];
        for (int i = 0; i < relativeLocations.size(); i++) {
            ret[i] = new MapLocation(
                    center.x + relativeLocations.get(i).x,
                    center.y + relativeLocations.get(i).y
            );
        }
        return ret;
    }


    /*
     * @return
     *      optimal location to move to get to location
     */
    public static MapLocation getOptimalLocation(
            MapLocation current, MapLocation destination, PassabilityGrid grid
    ) throws GameActionException {
        MapLocation[] circumference = getCircumferencePoints(current);
        double radians = Math.atan((destination.y-current.y) / Math.max(destination.x - current.x, 0.1));
        MapLocation minima = null;
        double maxFac = 0;
        for (MapLocation x: circumference) {
            double factor = Math.pow(grid.get(x), 0.1) /
                    Math.abs(radians - Math.atan((x.y-current.y) / Math.max(x.x - current.x, 0.1)));
            if (factor > maxFac) {
                minima = x;
                maxFac = factor;
            }
        }
        return minima;
    }
}
