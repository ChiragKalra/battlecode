package gen3;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import gen3.flags.MuckrakerFlag;
import gen3.util.DirectionFeeder;

import static gen3.RobotPlayer.*;
import static gen3.helpers.GridHelper.*;
import static gen3.helpers.MovementHelper.Precision;
import static gen3.helpers.MovementHelper.tryMove;


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
