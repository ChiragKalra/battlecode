package gen5;

import battlecode.common.*;
import gen5.helpers.GridHelper;
import gen5.util.EcInfo;

import static gen5.RobotPlayer.*;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.MovementHelper.*;

public strictfp class Muckraker {

    private static MapLocation getOptimalLocationToExpose () {
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius, enemyTeam);
        for (RobotInfo ri: nearby) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return ri.location;
            }
        }
        return null;
    }

    public static Direction getNextDirection() throws GameActionException {
        MapLocation mLoc = rc.getLocation();
        Direction grid = GridHelper.getDirectionFromAdjacentFlags(mLoc);
        return grid != null ? grid : getRandomDirection();
    }


    public static void move() throws GameActionException {
        MapLocation locToExpose;
        // movement
        locToExpose = getOptimalLocationToExpose();

        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (robot.type.canBeExposed()) {
                if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius)) {
                    // expose the slanderer
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                        return;
                    }
                } else {
                    locToExpose = robot.location;
                }
            }
        }


        if (locToExpose != null) {
            goTo(locToExpose);
        } else {
            EcInfo got = checkForAttackCoordinates();
            if (got != null && got.hp >= 0 && got.enemy) {
                goTo(got.location);
            } else {
                tryMove(getNextDirection(), false);
            }
        }
    }


    public static void init() { }
}
