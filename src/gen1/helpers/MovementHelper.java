package gen1.helpers;

import battlecode.common.*;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;

import static gen1.RobotPlayer.*;

import gen1.dataclasses.Pair;

@SuppressWarnings("unused")
public class MovementHelper {
    public static final int PRECISION_MAX = 422;
    public static final int PRECISION_MID = 325;
    public static final int PRECISION_LOW = 334;
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


    public static Direction getRandomDirection() {
        return (Direction) getRandom(directions);
    }

    public static MapLocation multiply(MapLocation got, Direction dir, int n) {
        MapLocation loc = new MapLocation(got.x, got.y);
        for (int i = 0; i < n; i++) {
            loc = loc.add(dir);
        }
        return loc;
    }

    public static boolean tryMove (Direction dir, int precision) throws GameActionException {
        // ideal for only precise movement
        if (precision == PRECISION_MAX) {
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
                int dirInt = Arrays.asList(directions).indexOf(dir);
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
     * returns null if there's no possible path to the destination
     *
     * passability = 0 for
     * - locations not on the map
     * - locations having robots
     * - locations outside of radius
     */
    public static ArrayList<Direction> getShortestRoute(RobotType robot, MapLocation destination, double[][] passability) {
        int size = passability.length;
        MapLocation source = new MapLocation(size / 2, size / 2);
        destination = new MapLocation(destination.x + size / 2, destination.y + size / 2);

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

        boolean visited[][] = new boolean[size][size];
        PriorityQueue<Pair<Double, MapLocation>> pq = new PriorityQueue<>((x, y) -> Double.compare(x.key, y.key));
        pq.add(new Pair(0, source));

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
                if (visited[loc.x][loc.y]) {
                    continue;
                }

                // TODO: handle passability = 0
                double edgeWeight = robot.actionCooldown / passability[cur.x][cur.y];
                if (distance[cur.x][cur.y] + edgeWeight < distance[loc.x][loc.y]) {
                    distance[loc.x][loc.y] = distance[cur.x][cur.y] + edgeWeight;
                    pq.add(new Pair(distance[loc.x][loc.y], new MapLocation(loc.x, loc.y)));
                    parent[loc.x][loc.y] = cur;
                }
            }
        }

        if (distance[destination.x][destination.y] >= INFINITY)
            return null;

        // build path
        ArrayList<Direction> route = new ArrayList<>();
        MapLocation loc = destination;
        while (loc != source)
        {
            route.add(parent[loc.x][loc.y].directionTo(loc));
            loc = parent[loc.x][loc.y];
        }
        Collections.reverse(route);
        return route;
    }
}
