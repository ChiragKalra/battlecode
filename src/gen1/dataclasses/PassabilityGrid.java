package gen1.dataclasses;

import battlecode.common.*;


import static gen1.RobotPlayer.*;


public class PassabilityGrid {


    public final int radiusSquared, radius, diameter;
    public final MapLocation center;

    private final Boolean[][] occupied;

    private static int minX = 0, maxX = Integer.MAX_VALUE, minY = 0, maxY = Integer.MAX_VALUE;

    private void addOutsideMap(MapLocation got) {
        if (got.x < center.x) {
            minX = Math.max(got.x, minX);
        } else if (center.x < got.x) {
            maxX = Math.min(got.x, maxX);
        }
        if (got.y < center.y) {
            minY = Math.max(got.y, minY);
        } else if (center.y < got.y) {
            maxY = Math.min(got.y, maxY);
        }
    }

    private boolean isOutsideMap(MapLocation ml) {
        return maxX <= ml.x || ml.x <= minX || maxY <= ml.y || ml.y <= minY;
    }

    public PassabilityGrid (MapLocation center, int radiusSquared) {
        this.radiusSquared = radiusSquared;
        this.center = center;
        this.radius = (int) Math.sqrt(radiusSquared);
        this.diameter = radius*2 + 1;
        this.occupied = new Boolean[diameter][diameter];
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

    public double get (MapLocation ml) throws GameActionException {
        if (ml.distanceSquaredTo(center) == 0) {
            return rc.sensePassability(ml);
        }
        if (!center.isWithinDistanceSquared(ml, radiusSquared) || isOutsideMap(ml)) {
            return 0;
        }
        int adjX = ml.x - center.x + radius, adjY = ml.y - center.y + radius;
        if (occupied[adjX][adjY] != null && occupied[adjX][adjY]) {
            return 0;
        }
        if (occupied[adjX][adjY] == null) {
            try {
                if (occupied[adjX][adjY] = rc.isLocationOccupied(ml)) {
                    return 0;
                }
            } catch (GameActionException e) {
                addOutsideMap(ml);
                return 0;
            }
        }
        return rc.sensePassability(ml);
    }
}
