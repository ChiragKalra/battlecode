package gen1.helpers;

import battlecode.common.*;
import gen1.dataclasses.PassabilityGrid;

import java.util.ArrayList;

import static gen1.RobotPlayer.*;


public class TerrainHelper {

    private static final int RADIUS_OPTIMAL_LOCATION = Math.min(sensorRadius, 9);

    /*
     *
     * @return
     *      a grid with passability values around the given map location
     *
     *      passability[x][y] = 0, for
     *          - locations not on the map
     *          - locations having robots
     *          - locations outside of radius
     */
    public static PassabilityGrid getPassabilityGrid (MapLocation current) throws GameActionException {
        int limX = (int) Math.sqrt(sensorRadius);
        double[][] grid = new double[2*limX+1][2*limX+1];
        MapLocation ml;
        grid[limX][limX] = rc.sensePassability(current);
        for (int x = -limX; x <= limX; x++) {
            int limY = (int) Math.sqrt(sensorRadius - x*x);
            for (int y = -limY; y <= limY; y++) {
                ml = new MapLocation(x+current.x, y+current.y);
                if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                    grid[x+limX][y+limX] = rc.sensePassability(ml);
                }
            }
        }
        return new PassabilityGrid(current, sensorRadius, grid);
    }


    /*
     * @return
     *      optimal location to move to in given direction
     */
    public static MapLocation getOptimalLocationInDirection(
            MapLocation current, Direction dir, PassabilityGrid grid
    ) throws GameActionException {
        int lim = (int) Math.sqrt(RADIUS_OPTIMAL_LOCATION), x, y, limX, limY;
        ArrayList<MapLocation> possible = new ArrayList<>();

        switch (dir) {
            case NORTH:
            case SOUTH:
                x = dir==Direction.NORTH ? lim : -lim;
                limY = (int) Math.sqrt(RADIUS_OPTIMAL_LOCATION - x*x);
                for (y = -limY; y <= limY; y++) {
                    MapLocation ml = new MapLocation(lim+current.x, y+current.y);
                    if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                        possible.add(ml);
                    }
                }
                break;

            case EAST:
            case WEST:
                y = dir==Direction.WEST ? lim : -lim;
                limX = (int) Math.sqrt(RADIUS_OPTIMAL_LOCATION - y*y);
                for (x = -limX; x <= limX; x++) {
                    MapLocation ml = new MapLocation(x+current.x, lim+current.y);
                    if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                        possible.add(ml);
                    }
                }
                break;

            case NORTHEAST:
            case SOUTHWEST:
            case SOUTHEAST:
            case NORTHWEST:
                for (x = 1; x < lim; x++) {
                    y = (int) Math.sqrt(RADIUS_OPTIMAL_LOCATION - x*x);
                    int fx = dir.dx*x , fy = dir.dy*y;
                    MapLocation ml = new MapLocation(fx+current.x, fy+current.y);
                    if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml) && current.directionTo(ml) == dir) {
                        possible.add(ml);
                    }
                }
        }

        MapLocation minima = null;
        double minPass = 0;
        for (MapLocation ml: possible) {
            if (grid.get(ml) > minPass) {
                minima = ml;
                minPass = grid.get(ml);
            }
        }

        return minima;
    }
}
