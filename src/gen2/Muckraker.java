package gen2;

import battlecode.common.*;
import gen2.flags.MuckrakerFlag;

import static gen2.RobotPlayer.*;
import static gen2.helpers.GridHelper.*;
import static gen2.helpers.MovementHelper.*;


public strictfp class Muckraker {
    public static boolean placed = false;
    public static MapLocation gridReferenceLocation = null;

    public static void move() throws GameActionException {
        // check for slanderers
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, mTeam)) {
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
            if (tryMove(getNextDirection(rc.getLocation()), Precision.MIN)) {
                MuckrakerFlag.updateFlagForEC();
                placed = formsGrid();
            }
        }
    }
}
