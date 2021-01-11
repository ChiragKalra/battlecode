package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.GridHelper.*;
import static gen1.helpers.MovementHelper.*;

/*
 # Muckraker Flag

 0-2,3   - 0-7, 1   -> placed with direction;
           1  , 0   -> placed without direction;
           0  , 0   -> searching;
 4-7     - politician approaching direction
 8-14    - enemy/neutral enlightenment center x
 15-21   - enemy/neutral enlightenment center y
 22      - attack/gird muckraker
 23      - undecided

 # 3-Bit Direction
    RobotPlayer.Directions 3-Bit integer indexed

 # 4-Bit Direction
  0 - North
  1 - East
  2 - South
  3 - West

 */

public strictfp class Muckraker {
    public static boolean placed = false;
    public static MapLocation gridReferenceLocation = null;

    private static void setEnlightenmentCenterLocation() {
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                spawnerLocation = gridReferenceLocation = ri.location;
                break;
            }
        }
    }

    public static void move() throws GameActionException {
        // occupy a grid spot if not unplaced
        if (!placed) {
            setEnlightenmentCenterLocation();
            if (tryMove(getNextDirection(rc.getLocation()), Precision.MIN)) {
                placed = formsGrid();
            }
        }

        RobotInfo[] afterMoveNearby = rc.senseNearbyRobots(sensorRadius, mTeam);

        // check for slanderers
        for (RobotInfo robot : afterMoveNearby) {
            if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius) && robot.type.canBeExposed()) {
                // expose the slanderer
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = 0;

        // set vacant grid location direction
        if (placed) {
            Direction direction = getGridDirectionForFlag();
            if (direction == null) {
                newFlag += 1;
            } else {
                int threeBit = directionList.indexOf(direction);
                newFlag += threeBit + 8;
            }
        }

        // set enemy/neutral enlightenment center location
        if (spawnerLocation != null && checkForEnemyEnlightenmentCenter() != null) {
            int relX = enemyEnlightenmentCenter.x - spawnerLocation.x + 63,
                    relY = enemyEnlightenmentCenter.y - spawnerLocation.y + 63;

            newFlag += relX << 8;
            newFlag += relY << 15;
        } else {
            // set location to 0,0
            newFlag += (63 << 8) + (63 << 15);
        }

        // update
        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }

        if (DEBUG) {
            float k = 1f;
            if (Clock.getBytecodeNum() > 1000*k) {
                System.out.println("ByteCodes Used over " + k + "k: " + Clock.getBytecodeNum());
            }
        }
    }
}
