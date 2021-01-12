package gen1.dataclasses;

import battlecode.common.*;


import static gen1.RobotPlayer.*;

@SuppressWarnings("unused")
public class PassabilityGrid {

    public final int radiusSquared, radius, diameter;
    public final MapLocation center;

    private final boolean[][] occupied;

    public PassabilityGrid (MapLocation center, int radiusSquared) {
        this.radiusSquared = radiusSquared;
        this.center = center;
        this.radius = (int) Math.sqrt(radiusSquared);
        this.diameter = radius*2 + 1;

        this.occupied = new boolean[diameter][diameter];

        for (MapLocation ml: rc.detectNearbyRobots(radiusSquared)) {
            int adjX = ml.x - center.x + radius, adjY = ml.y - center.y + radius;
            occupied[adjX][adjY] = true;
        }
    }

    public double get (int x, int y) throws GameActionException {
        return get(new MapLocation(x, y));
    }

    public double getRelative (int x, int y) throws GameActionException {
        return get(new MapLocation(x+center.x, y+center.y));
    }

    public double getIndexed (int x, int y) throws GameActionException {
        return get(new MapLocation(x-radius+center.x, y-radius+center.y));
    }

    public boolean isBlockedOrOutsideRelative (int x, int y) throws GameActionException {
        return isBlockedOrOutside(new MapLocation(x+center.x, y+center.y));
    }

    public boolean isBlockedOrOutside (MapLocation ml) throws GameActionException {
        if (center.distanceSquaredTo(ml) > sensorRadius) {
            return true;
        }
        if (!rc.onTheMap(ml)) {
            return true;
        }
        return occupied[ml.x - center.x + radius][ml.y - center.y + radius];
    }

    public double get (MapLocation ml) throws GameActionException {
        if (ml.distanceSquaredTo(center) == 0) {
            return rc.sensePassability(ml);
        }
        if (!center.isWithinDistanceSquared(ml, radiusSquared)) {
            return 0;
        }
        if (!rc.onTheMap(ml)) {
            return 0;
        }
        if (occupied[ml.x - center.x + radius][ml.y - center.y + radius]) {
            return 0;
        }
        return rc.sensePassability(ml);
    }
}
