package gen1;

import battlecode.common.*;

import java.util.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.GridHelper.*;
import static gen1.helpers.MovementHelper.*;

/*
 # Muckraker Flag

 0      - searching/placed
 1-4    - politician approaching direction
 5-7    - vacant muckraker spot direction
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

    static void setEnlightenmentCenterLocation(RobotInfo[] nearby) {
        if (gridReferenceLocation == null) {
            for (RobotInfo ri: nearby) {
                if (ri.team == mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    gridReferenceLocation = ri.location;
                    break;
                }
            }
        }
    }

    static void move() throws GameActionException {
        // occupy a grid spot if not unplaced
        RobotInfo[] afterMoveNearby = null;
        if (!placed) {
            RobotInfo[] fellow = rc.senseNearbyRobots(sensorRadius, mTeam);
            setEnlightenmentCenterLocation(fellow);
            Direction init = getInitDirection(fellow), decided;
            if (init == null) {
                Direction vacant = checkVacantSpot();
                if (vacant != null) {
                    decided = vacant;
                } else {
                    ArrayList<Direction> selected = new ArrayList<>();
                    for (RobotInfo ri : fellow) {
                        int flag = rc.getFlag(ri.getID());
                        if (ri.type == RobotType.MUCKRAKER && isPlaced(flag)) {
                            selected.add(getDirection(flag));
                        }
                    }
                    decided = selected.isEmpty() ? getRandomDirection() : //replace with better algo
                    (Direction) getRandom(selected.toArray());
                }
            } else {
                decided = init;
            }

            if (tryMove(decided, PRECISION_LOW)) {
                afterMoveNearby = rc.senseNearbyRobots(sensorRadius, mTeam);
                placed = formsGrid(afterMoveNearby);
            }
        }
        // save bytecode with re-usage
        if (afterMoveNearby == null) {
            afterMoveNearby = rc.senseNearbyRobots(sensorRadius, mTeam);
        }

        // check for slanderers
        for (RobotInfo robot: afterMoveNearby) {
            if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius) && robot.type.canBeExposed()) {
                // expose the slanderer
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }

        // check for flag changes and set flag
        int prevFlag = rc.getFlag(rc.getID()), newFlag = placed ? 1 : 0;
        if (placed) {
            int threeBit = directionList.indexOf(getVacantDirection());
            newFlag += threeBit << 5;
        }

        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }

        if (DEBUG) {
            float k = 5;
            if (Clock.getBytecodeNum() > 1000*k) {
                System.out.println("ByteCodes Used over " + k + "k: " + Clock.getBytecodeNum());
            }
        }
    }
}
