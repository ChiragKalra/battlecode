package gen1;

import battlecode.common.*;

import java.util.*;

@SuppressWarnings("unused")
public strictfp class RobotPlayer {
    static final int PRECISION_MAX = 422;
    static final int PRECISION_MID = 325;
    static final int PRECISION_LOW = 334;

    static final int MAX_GENERATED_INFLUENCE = 22364;


    static RobotController rc;
    static int round;
    static Team mTeam, enemyTeam;
    static int actionRadius, sensorRadius;

    static MapLocation spawnLocation;

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static MapLocation multiply(MapLocation got, Direction dir, int n) {
        MapLocation loc = new MapLocation(got.x, got.y);
        for (int i = 0; i < n; i++) {
            loc = loc.add(dir);
        }
        return loc;
    }

    static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    static Direction getRandomDirection() {
        return (Direction) getRandom(directions);
    }

    static boolean tryMove(Direction dir, int precision) throws GameActionException {
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

    public static void run (RobotController robotController) {
        rc = robotController;
        round = rc.getRoundNum();
        mTeam = rc.getTeam();
        enemyTeam = mTeam.opponent();
        actionRadius = rc.getType().actionRadiusSquared;
        sensorRadius = rc.getType().sensorRadiusSquared;

        // store spawn/ switch location
        spawnLocation = rc.getLocation();

        while (true) {
            round++;
            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        EnlightenmentCenter.move();
                        break;
                    case POLITICIAN:
                        Politician.move();
                        break;
                    case SLANDERER:
                        Slanderer.move();
                        break;
                    case MUCKRAKER:
                        Muckraker.move();
                        break;
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
