package gen3;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import gen3.flags.MuckrakerFlag;

import static gen3.RobotPlayer.*;
import static gen3.helpers.GridHelper.*;
import static gen3.helpers.MovementHelper.tryMove;


public strictfp class Muckraker {
    public static boolean placed = false;

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
            Direction dir = getDirectionsToVacancy();
            if (dir != null) {
                tryMove(dir);
            } else {
                dir = getNextDirection();
                if (dir != null) {
                    tryMove(dir);
                }
            }
            placed = formsGrid();
        }
    }
}
