package gen1;

import battlecode.common.*;

import java.util.ArrayList;

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
  3 - west

 */


public strictfp class Muckraker {

    private static final int MUCKRAKER_PLACED = 1;

    static boolean placed = false;

    static boolean isPlaced (int flag) {
        return (flag & MUCKRAKER_PLACED) > 0;
    }

    static Direction getDirection(int flag) {
        return directions[(flag >> 5) % 8];
    }

    static Boolean formsGrid (RobotInfo ri) {
        MapLocation placedLocation = ri.location, mLocation = rc.getLocation();
        boolean yDif = Math.abs(placedLocation.y - mLocation.y) == 5,
                xDif = Math.abs(placedLocation.x - mLocation.x) == 5;
        if (placedLocation.x == mLocation.x) {
            return yDif;
        } else if (placedLocation.y == mLocation.y) {
            return xDif;
        } else {
            return yDif && xDif;
        }
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
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
             Direction decided = (Direction) getRandom(selected.toArray());

             if (tryMove(decided)) {
                 afterMoveNearby = rc.senseNearbyRobots(sensorRadius);
                 for (RobotInfo ri: fellow) {
                     int flag = rc.getFlag(ri.getID());
                     if (ri.team == mTeam && ri.type == RobotType.MUCKRAKER && isPlaced(flag)) {
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

         for (RobotInfo robot: afterMoveNearby) {
             // check for slanderers
             if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius) && robot.type.canBeExposed()) {
                 // expose the slanderer
                 if (rc.canExpose(robot.location)) {
                     rc.expose(robot.location);
                     return;
                 }
             }
         }

         // check for flag changes and set flag

    }
}
