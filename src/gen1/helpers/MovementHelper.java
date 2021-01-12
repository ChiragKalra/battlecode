package gen1.helpers;

import battlecode.common.*;

import java.util.*;

import static gen1.RobotPlayer.*;


import gen1.dataclasses.Pair;
import gen1.dataclasses.PassabilityGrid;

public class MovementHelper {

    // max acceptable crowding ratio in a direction
    public static final double RATIO_CROWDING = 0.33;

    // movement precision
    @SuppressWarnings("unused")
    public enum  Precision {
        MAX,
        MID,
        MIN
    }
  
    public static final double INFINITY = 10000;

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
        return (Direction) getRandom(directions);
    }

    public static MapLocation clone(MapLocation d) {
        return new MapLocation(d.x, d.y);
    }

    public static MapLocation multiply(MapLocation got, Direction dir, int n) {
        MapLocation loc = clone(got);
        for (int i = 0; i < n; i++) {
            loc = loc.add(dir);
        }
        return loc;
    }

    private static float[] convolveCircularly (float[] array, float[] filter) {
        int sz = array.length, fsz = filter.length;
        float[] out = new float[sz];
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < fsz; j++) {
                out[i] += filter[j] * array[Math.floorMod(j-fsz/2+i, sz)];
            }
        }
        return out;
    }

    public static Direction getAntiCrowdingDirection(MapLocation current) throws GameActionException {
        byte[] occupied = new byte[8], total = new byte[8];
        int limX = (int) Math.sqrt(detectionRadius);
        PassabilityGrid grid = new PassabilityGrid(current, detectionRadius);
        for (int x = -limX; x <= limX; x++) {
            int limY = (int) Math.sqrt(detectionRadius - x*x);
            for (int y = -limY; y <= limY; y++) {
                MapLocation ml = new MapLocation(x+current.x, y+current.y);
                int dirInd = directionList.indexOf(current.directionTo(ml));
                //  don't evaluate x = y = 0 condition
                if (dirInd != -1) {
                    total[dirInd]++;
                    occupied[dirInd] += grid.isBlockedOrOutside(ml) ? 1 : 0;
                }
            }
        }

        float[] ratios = new float[8], filter = {0.05f, .2f, .5f, .2f, .05f};
        for (int i = 0; i < 8; i++) {
            ratios[i] = occupied[i] / (float) total[i];
        }
        ratios = convolveCircularly(ratios, filter);

        int maxInd = -1;
        float maxRatio = 0;
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

    public static boolean tryMove (Direction dir, Precision precision) throws GameActionException {
        // ideal for only precise movement
        if (precision == Precision.MAX) {
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            } else {
                return false;
            }
        } else {
            // optimise for shorter and longer movements
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            } else if (rc.getCooldownTurns() < 1) {
                int dirInt = directionList.indexOf(dir);
                // if blocked by another robot, find the next best direction
                for (int i = 1; i<5; i++) {
                    if (Math.random() < 0.5) {
                        Direction got = directions[Math.floorMod(dirInt + i, 8)];
                        if (rc.canMove(got)) {
                            rc.move(got);
                            return true;
                        }
                    } else {
                        Direction got = directions[Math.floorMod(dirInt - i, 8)];
                        if (rc.canMove(got)) {
                            rc.move(got);
                            return true;
                        }
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }


    /*
     * @param
     *      current: source coordinates
     *      destination: destination coordinates
     *      passability: grid of passability values around the robot
     *
     * @return
     *      ArrayList where Direction order is reversed,
     *      null if there's no possible path to the destination
     */
    public static ArrayList<Direction> getShortestRoute(
            MapLocation current, MapLocation destination, PassabilityGrid passability
    ) throws GameActionException {
        int size = passability.diameter;
        MapLocation source = new MapLocation(size / 2, size / 2);
        destination = new MapLocation(destination.x + size / 2 - current.x, destination.y + size / 2 - current.y);

        double[][] distance = new double[size][size];
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                distance[x][y] = INFINITY;
            }
        }
        distance[source.x][source.y] = 0;

        MapLocation[][] parent = new MapLocation[size][size];
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                parent[x][y] = new MapLocation(-1, -1);
            }
        }

        boolean[][] visited = new boolean[size][size];
        PriorityQueue<Pair<Double, MapLocation>> pq = new PriorityQueue<>(Comparator.comparingDouble(x -> x.key));
        pq.add(new Pair<>(0d, source));

        int[] dx = {0, 0, 1, 1, 1, -1, -1, -1};
        int[] dy = {1, -1, 0, 1, -1, 0, 1, -1};

        while (!pq.isEmpty()) {
            MapLocation cur = pq.poll().value;
            if (visited[cur.x][cur.y]) {
                continue;
            }

            visited[cur.x][cur.y] = true;
            for (int i = 0; i < 8; ++i) {
                MapLocation loc = new MapLocation(cur.x + dx[i], cur.y + dy[i]);
                if (loc.x < 0 || loc.x >= size || loc.y < 0 || loc.y >= size) {
                    continue;
                }
                if (visited[loc.x][loc.y] || passability.getIndexed(loc.x, loc.y) == 0) {
                    continue;
                }

                double edgeWeight = mType.actionCooldown / passability.getIndexed(cur.x, cur.y);
                if (distance[cur.x][cur.y] + edgeWeight < distance[loc.x][loc.y]) {
                    distance[loc.x][loc.y] = distance[cur.x][cur.y] + edgeWeight;
                    pq.add(new Pair<>(distance[loc.x][loc.y], new MapLocation(loc.x, loc.y)));
                    parent[loc.x][loc.y] = cur;
                }
            }
        }

        if (distance[destination.x][destination.y] >= INFINITY) {
            return null;
        }

        // build path
        ArrayList<Direction> route = new ArrayList<>();
        MapLocation loc = destination;
        while (loc != source) {
            route.add(parent[loc.x][loc.y].directionTo(loc));
            loc = parent[loc.x][loc.y];
        }
        return route;
    }


}
