package gen3.helpers;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen3.util.Logger;
import gen3.util.PassabilityGrid;

import java.util.ArrayList;

import static gen3.RobotPlayer.actionRadius;
import static gen3.RobotPlayer.sensorRadius;


public class TerrainHelper {

    private static ArrayList<MapLocation> relativeLocations;
    private static int cachedRadius = 0;

    public static MapLocation[] getCircumferencePoints(MapLocation center, int radiusSquared) {
        int rad = (int) Math.sqrt(radiusSquared);
        if (relativeLocations == null || cachedRadius != radiusSquared) {
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
            cachedRadius = radiusSquared;
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
}
