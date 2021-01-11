package gen1;

import battlecode.common.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.GridHelper.*;
import static gen1.helpers.MovementHelper.*;

/*
 # Muckraker Flag

 0-2, 3 - 0-7, 1   -> placed with direction;
          1  , 0   -> placed without direction;
          0  , 0   -> searching;
 4-7    - politician approaching direction
 8-14   - enemy/neutral enlightenment center x
 15-21  - enemy/neutral enlightenment center y
 22     - attack/gird muckraker
 23     - undecided

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
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius, mTeam);
        if (gridReferenceLocation == null) {
            for (RobotInfo ri: nearby) {
                if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    gridReferenceLocation = ri.location;
                    break;
                }
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
        if (placed) {
            Direction direction = getGridDirectionForFlag();
            if (DEBUG) {
                System.out.println(direction != null ? direction.name(): "null direction");
            }
            if (direction == null) {
                newFlag += 1;
            } else {
                int threeBit = directionList.indexOf(direction);
                newFlag += threeBit + 8;
            }
        }

        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }

        if (DEBUG) {
            float k = 5f;
            if (Clock.getBytecodeNum() > 1000*k) {
                System.out.println("ByteCodes Used over " + k + "k: " + Clock.getBytecodeNum());
            }
        }
    }
}
