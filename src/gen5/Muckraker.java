package gen5;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import gen5.util.Pair;

import static gen5.RobotPlayer.*;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.MovementHelper.*;

public strictfp class Muckraker {

    public static void move() throws GameActionException {
        MapLocation locToEmp;
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (robot.type.canBeExposed()) {
                if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius)) {
                    // expose the slanderer
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                        return;
                    }
                } else {
                    locToEmp = robot.location;
                }
            }
        }

        // movement
        locToEmp = getOptimalLocationToEmpower();

        if (locToEmp != null) {
            goTo(locToEmp);
        } else {
            Pair<MapLocation, Integer> got = checkForAttackCoordinates();
            if (got != null && got.value >= 0) {
                goTo(got.key);
            } else {
                tryMove(getNextDirection(null), false);
            }
        }
    }


    public static void init() { }
}
