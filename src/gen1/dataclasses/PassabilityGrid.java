package gen1.dataclasses;

import battlecode.common.MapLocation;


public class PassabilityGrid {
    public final int radiusSquared, radius, diameter;
    public final double[][] grid;
    public final MapLocation center;

    public PassabilityGrid (MapLocation center, int radiusSquared, double[][] grid) {
        this.grid = grid;
        this.radiusSquared = radiusSquared;
        this.center = center;
        this.diameter = grid.length;
        this.radius = diameter / 2;
    }

    public double get (int x, int y) {
        return get(new MapLocation(x, y));
    }

    public double get (MapLocation ml) {
        if (!center.isWithinDistanceSquared(ml, radiusSquared)) {
            return 0;
        }
        return grid[ml.x-center.x+radius][ml.y-center.y+radius];
    }
}
