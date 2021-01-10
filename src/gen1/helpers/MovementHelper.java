package gen1.helpers;

import battlecode.common.*;

import java.util.*;

import static gen1.RobotPlayer.*;

public class MovementHelper {

    // max acceptable crowding ratio in a direction
    public static final double RATIO_CROWDING = 0.33;

    // movement precision
    public static final int PRECISION_MAX = 422;
    public static final int PRECISION_MID = 325;
    public static final int PRECISION_LOW = 334;

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

    public static Direction getAntiCrowdingDirection(MapLocation current) {
        byte[] occupied = new byte[8], total = new byte[8];
        int limX = (int) Math.sqrt(detectionRadius), dirInd;
        MapLocation ml;
        for (int x = -limX; x <= limX; x++) {
            int limY = (int) Math.sqrt(detectionRadius - x*x);
            for (int y = -limY; y <= limY; y++) {
                ml = new MapLocation(x+current.x, y+current.y);
                dirInd = directionList.indexOf(current.directionTo(ml));
                if (dirInd == -1) {
                    // x = y = 0 condition, don't evaluate
                    continue;
                }
                total[dirInd]++;
                try {
                    occupied[dirInd] += rc.isLocationOccupied(ml) ? 1 : 0;
                } catch (GameActionException e) {
                    // location is outside the map, count as occupied
                    occupied[dirInd] += 1;
                }
            }
        }
        float maxRatio = 0, ratio;
        int maxInd = -1;
        for (int i = 0; i < 8; i++) {
            ratio = occupied[i] / (float) total[i];
            if (ratio > maxRatio) {
                maxRatio = ratio;
                maxInd = i;
            }
        }
        if (maxRatio <= RATIO_CROWDING) {
            return null;
        }
        return directions[(maxInd+4)%8];
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
}
