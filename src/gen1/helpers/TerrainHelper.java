package gen1.helpers;

import battlecode.common.*;

import static gen1.RobotPlayer.*;


public class TerrainHelper {

    /*
     *
     * @return a grid with passability values around the given map location
     *
     * passability = 0 for
     * - locations not on the map
     * - locations having robots
     * - locations outside of radius
     */
    public static double[][] getPassabilityGrid (MapLocation current) throws GameActionException {
        int limX = (int) Math.sqrt(sensorRadius);
        double[][] grid = new double[2*limX+1][2*limX+1];
        MapLocation ml;
        for (int x = -limX; x <= limX; x++) {
            int limY = (int) Math.sqrt(sensorRadius - x*x);
            for (int y = -limY; y <= limY; y++) {
                ml = new MapLocation(x+current.x, y+current.y);
                if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                    grid[x+limX][y+limX] = rc.sensePassability(ml);
                }
            }
        }
        return grid;
    }
}
