package gen2.helpers;

import battlecode.common.*;
import gen2.util.PassabilityGrid;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.helpers.MovementHelper.directions;


public class TerrainHelper {

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

    private static MapLocation getVacantNearby (
            MapLocation current, MapLocation destination, PassabilityGrid grid
    ) throws GameActionException {
        Direction direction = destination.directionTo(current);

        int dirInd = directionList.indexOf(direction), decided = -1;

        double factor = 0;

        for (int i = 0; i < 8; i++) {
            Direction d = directions[i];
            int filterInd = Math.min(Math.abs(dirInd-i), Math.abs(dirInd - 8 + i));
            double cur = DIRECTION_FACTOR[filterInd]*grid.getRelative(d.dx, d.dy);
            if (cur > factor) {
                decided = i;
                factor = cur;
            }
        }
        if (decided == -1) {
            return null;
        }

        return destination.add(directions[decided]);
    }

    private static MapLocation getIntermediate (
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
            // find high passability location if destination inside sensor radius
            return getVacantNearby(current, destination, grid);
        }
    }
}
