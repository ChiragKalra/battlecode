package gen3.helpers;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen3.util.PassabilityGrid;

import java.util.ArrayList;

import static gen2.RobotPlayer.sensorRadius;


public class TerrainHelper {

    private static ArrayList<MapLocation> relativeLocations;

    public static MapLocation[] getCircumferencePoints(MapLocation center, int radiusSquared) {
        int rad = (int) Math.sqrt(radiusSquared);
        if (relativeLocations == null) {
            relativeLocations = new ArrayList<>();
            for (int x = -rad; x <= rad; x++) {
                int limY = (int) Math.sqrt(radiusSquared - x*x);
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

    private static MapLocation getIntermediate (
            MapLocation current, MapLocation destination, PassabilityGrid grid
    ) throws GameActionException {
        MapLocation[] circumference = getCircumferencePoints(current, sensorRadius);
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


    /*
     * @return
     *      optimal location to move to get to location
     */
    public static MapLocation getOptimalLocation (
            MapLocation current, MapLocation destination, PassabilityGrid grid
    ) throws GameActionException {
        if (current.distanceSquaredTo(destination) > sensorRadius) {
            // find intermediate location if destination outside sensor radius
            return getIntermediate(current, destination, grid);
        } else {
            return null;
        }
    }
}
