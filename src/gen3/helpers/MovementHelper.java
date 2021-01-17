package gen3.helpers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import gen3.util.Functions;
import gen3.util.Pair;
import gen3.util.PassabilityGrid;

import java.util.*;

import static gen2.RobotPlayer.actionRadius;
import static gen2.RobotPlayer.rc;
import static gen2.util.Functions.convolveCircularly;

public class MovementHelper {

    // max acceptable crowding ratio in a direction
    public static final double RATIO_CROWDING = 0.33;
    public static final int RADIUS_CROWDING = actionRadius;

    // adjacent direction preference factor
    public static final double[] DIRECTION_FACTOR = {.5, .25, .125, .0625, 0.03125};

    // movement precision
    @SuppressWarnings("unused")
    public enum Precision {
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
        return (Direction) Functions.getRandom(directions);
    }

    public static MapLocation clone(MapLocation d) {
        return new MapLocation(d.x, d.y);
    }

    public static Direction vectorAddition(Direction ... dirs) {
        MapLocation yeah = new MapLocation(0,0);
        for (Direction d : dirs) {
            yeah = yeah.add(d);
        }
        return (new MapLocation(0,0)).directionTo(yeah);
    }

    public static MapLocation multiply(MapLocation loc, Direction dir, int n) {
        return clone(loc).translate(dir.dx*n, dir.dy*n);
    }

    public static Direction getAntiCrowdingDirection(MapLocation current) throws GameActionException {
        byte[] occupied = new byte[8], total = new byte[8];
        int rad = (int) Math.sqrt(RADIUS_CROWDING);
        PassabilityGrid grid = new PassabilityGrid(current, RADIUS_CROWDING);
        for (int x = -rad; x <= rad; x++) {
            int limY = (int) Math.sqrt(RADIUS_CROWDING - x*x);
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

        double[] ratios = new double[8], filter = {0.05, .2, .5, .2, .05};
        for (int i = 0; i < 8; i++) {
            ratios[i] = occupied[i] / (float) total[i];
        }
        ratios = convolveCircularly(ratios, filter);

        int maxInd = -1;
        double maxRatio = 0;
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
            }
        }
        return false;
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
        int srcToDesInd = directionList.indexOf(current.directionTo(destination));
        MapLocation source = new MapLocation(size / 2, size / 2);
        destination = new MapLocation(destination.x + size / 2 - current.x, destination.y + size / 2 - current.y);

        Double[][] distance = new Double[size][size];
        MapLocation[][] parent = new MapLocation[size][size];
        boolean[][] visited = new boolean[size][size];
        PriorityQueue<Pair<Double, MapLocation>> pq = new PriorityQueue<>(Comparator.comparingDouble(x -> x.key));

        distance[source.x][source.y] = 0d;
        pq.add(new Pair<>(0d, source));

        while (!pq.isEmpty()) {
            MapLocation cur = pq.poll().value;
            if (visited[cur.x][cur.y]) {
                continue;
            }

            visited[cur.x][cur.y] = true;
            for (int i = -1; i < 1; ++i) {
                Direction d = directions[Math.floorMod(srcToDesInd+i, 8)];
                MapLocation loc = new MapLocation(cur.x + d.dx, cur.y + d.dy);
                if (loc.x < 0 || loc.x >= size || loc.y < 0 || loc.y >= size) {
                    continue;
                }
                if (visited[loc.x][loc.y] || passability.getIndexed(loc.x, loc.y) == 0) {
                    continue;
                }

                // to reduce cross-walks
                // double diagonalFactor = Math.abs(dx[i]) == Math.abs(dy[i]) ? DIAGONAL_MOVEMENT_REDUCTION_FACTOR : 1;
                double edgeWeight = 1 / passability.getIndexed(cur.x, cur.y), curDis = distance[cur.x][cur.y];
                if (distance[loc.x][loc.y] == null) {
                    distance[loc.x][loc.y] = INFINITY;
                }
                if (curDis + edgeWeight < distance[loc.x][loc.y]) {
                    double dis = distance[loc.x][loc.y] = curDis + edgeWeight;
                    pq.add(new Pair<>(dis, loc));
                    parent[loc.x][loc.y] = cur;
                }
            }
        }

        if (distance[destination.x][destination.y] == null || distance[destination.x][destination.y] >= INFINITY) {
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
