package gen5;

import battlecode.common.*;

import static gen5.RobotPlayer.*;
import static gen5.helpers.GridHelper.*;
import static gen5.helpers.MovementHelper.*;


public strictfp class GridPolitician {
    public static boolean placed = false;
    private static int explodeRadius = 0;
    private static Direction spawnDirection = null;

    public static void move() throws GameActionException {
        if (rc.detectNearbyRobots().length >= 75) {
            rc.empower(1);
            return;
        }

        // occupy a grid spot if not unplaced

        if (explodeRadius == 0 && spawnerLocation != null) {
            spawnDirection = spawnerLocation.directionTo(rc.getLocation());
            int ind = directionList.indexOf(spawnDirection);
            spawnDirection = directions[(ind/2)*2];
            if (roundNumber < 30) {
                explodeRadius = 10;
            }
        }

        RobotInfo[] fellow = rc.senseNearbyRobots(sensorRadius, mTeam);

        if (explodeRadius < 13 && spawnDirection != null) {
            if (!rc.onTheMap(rc.getLocation().add(spawnDirection))) {
                spawnDirection = spawnDirection.rotateLeft();
            }
            tryMove(spawnDirection, false);
            explodeRadius++;
        } else if (!placed) {
            Direction dir = getDirectionsToVacancy();
            if (dir != null) {
                tryMove(dir, true);
            } else {
                dir = getNextDirection(fellow);
                if (dir != null) {
                    tryMove(dir, true);
                }
            }
            placed = formsGrid();
        }
    }
}
