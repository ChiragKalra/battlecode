package gen1;

import battlecode.common.*;

import java.util.*;


@SuppressWarnings("unused")
public strictfp class RobotPlayer {
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

    static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    static Direction randomDirection() {
        return (Direction) getRandom(directions);
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
