package gen2;

import battlecode.common.*;
import gen2.flags.MuckrakerFlag;
import gen2.util.DirectionFeeder;

import static gen2.RobotPlayer.*;
import static gen2.helpers.GridHelper.*;
import static gen2.helpers.MovementHelper.*;


public strictfp class Muckraker {
    public static boolean placed = false;

    private static DirectionFeeder routeToVacancy;

    public static void move() throws GameActionException {
        // check for slanderers
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius) && robot.type.canBeExposed()) {
                // expose the slanderer
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }

        // occupy a grid spot if not unplaced
        if (!placed) {
            if (routeToVacancy == null || !routeToVacancy.hasNext()) {
                routeToVacancy = getDirectionsToVacancy();
            }
            Direction next;
            if (routeToVacancy != null && routeToVacancy.hasNext()) {
                next = routeToVacancy.getNext();
            } else {
                next = getNextDirection();
            }
            if (next != null) {
                if (tryMove(next, Precision.MIN)) {
                    MuckrakerFlag.updateFlagForEC();
                    placed = formsGrid();
                }
            }
        }

        if (placed) {
            MuckrakerFlag.updateFlagIfECNearby();
        }
    }
}
