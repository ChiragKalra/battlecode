package gen1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

import static gen1.RobotPlayer.*;

/*
 # Muckraker Flag

 0      - searching/placed
 1-4    - politician approaching direction
 5-7    - vacant muckraker spot direction
 8-10   - enemy/neutral enlightenment center direction
 11-14  - enemy/neutral enlightenment center distance (0 is infinity)
 15-24  - undecided

 # 3-Bit Direction
    RobotPlayer.Directions 3-Bit integer indexed

 # 4-Bit Direction
  0 - North
  1 - East
  2 - South
  3 - West

 */


public strictfp class Muckraker {

    private static final int MUCKRAKER_PLACED = 1;
    private static final int MUCKRAKER_GRID_WIDTH = 5;

    static boolean placed = false;

    static boolean isPlaced (int flag) {
        return (flag & MUCKRAKER_PLACED) > 0;
    }

    static Direction getDirection(int flag) {
        return directions[(flag >> 5) % 8];
    }

    static Direction getInitDirection(RobotInfo[] nearby) {
        for (RobotInfo ri: nearby) {
            if (ri.team == mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return ri.location.directionTo(rc.getLocation());
            }
        }
        return getRandomDirection();
    }

    static Direction getVacantDirection(RobotInfo[] nearby) throws GameActionException {
        ArrayList<Direction> selected = new ArrayList<>();
        for (RobotInfo ri: nearby) {
            int flag = rc.getFlag(ri.getID());
            if (ri.team == mTeam && ri.type == RobotType.MUCKRAKER && isPlaced(flag)) {
                selected.add(getDirection(rc.getFlag(rc.getID())));
            }
        }
        if (selected.isEmpty()) {
            return getRandomDirection();
        } else {
            return (Direction) getRandom(selected.toArray());
        }
    }

    static Boolean formsGrid (RobotInfo ri) {
        MapLocation placedLocation = ri.location, mLocation = rc.getLocation();
        boolean yDif = Math.abs(placedLocation.y - mLocation.y) == MUCKRAKER_GRID_WIDTH,
                xDif = Math.abs(placedLocation.x - mLocation.x) == MUCKRAKER_GRID_WIDTH;
        if (placedLocation.x == mLocation.x) {
            return yDif;
        } else if (placedLocation.y == mLocation.y) {
            return xDif;
        } else {
            return yDif && xDif;
        }
    }

     static void move() throws GameActionException {
         // occupy a grid spot if not unplaced
         RobotInfo[] afterMoveNearby = null;
         if (!placed) {
             RobotInfo[] fellow = rc.senseNearbyRobots(sensorRadius);
             ArrayList<Direction> selected = new ArrayList<>();
             for (RobotInfo ri: fellow) {
                 int flag = rc.getFlag(ri.getID());
                 if (ri.team == mTeam && ri.type == RobotType.MUCKRAKER && isPlaced(flag)) {
                     selected.add(getDirection(flag));
                 }
             }
             Direction decided = selected.isEmpty() ? getInitDirection(fellow) :
                     (Direction) getRandom(selected.toArray());

             if (tryMove(decided)) {
                 afterMoveNearby = rc.senseNearbyRobots(sensorRadius);
                 for (RobotInfo ri: afterMoveNearby) {
                     int flag = rc.getFlag(ri.getID());
                     if (ri.team == mTeam &&
                             (ri.type == RobotType.MUCKRAKER && isPlaced(flag) || ri.type == RobotType.ENLIGHTENMENT_CENTER)) {
                         if (formsGrid(ri)) {
                             placed = true;
                             break;
                         }
                     }
                 }
             }
         }

         // save bytecode with re-usage
         if (afterMoveNearby == null) {
             afterMoveNearby = rc.senseNearbyRobots(sensorRadius);
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
             int threeBit = Arrays.asList(directions).indexOf(getVacantDirection(afterMoveNearby));
             newFlag += threeBit << 5;
         }

         if (newFlag != prevFlag) {
             rc.setFlag(newFlag);
         }
    }
}
