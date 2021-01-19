package gen4;

import battlecode.common.*;

import static gen4.RobotPlayer.*;
import static gen4.helpers.GridHelper.*;
import static gen4.helpers.MovementHelper.tryMove;


public strictfp class Muckraker {
    public static boolean placed = false;
    private static int explodeRadius = 0;
    private static Direction spawnDirection = null;

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


        /*
        not making enough muckrakers to block spawn
        MapLocation got = AttackHelper.getNearbyEnemyEc();
        if (got != null) {
            tryMove(rc.getLocation().directionTo(got));
            return;
        }*/

        // occupy a grid spot if not unplaced

        if (explodeRadius == 0 && spawnerLocation != null) {
            spawnDirection = spawnerLocation.directionTo(rc.getLocation());
        }

        if (explodeRadius < 4*5 && spawnDirection != null) {
            tryMove(spawnDirection);
            explodeRadius++;
        } else if (!placed) {
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
