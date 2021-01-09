package gen1.helpers;

import battlecode.common.*;

import java.util.Arrays;

import static gen1.RobotPlayer.*;

@SuppressWarnings("unused")
public class MovementHelper {
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
}
