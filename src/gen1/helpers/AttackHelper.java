package gen1.helpers;

import battlecode.common.*;

import gen1.dataclasses.PassabilityGrid;

import java.util.*;

import static gen1.RobotPlayer.*;
import static gen1.helpers.GridHelper.getCoordinatesFromFlag;
import static gen1.helpers.GridHelper.isPlaced;
import static gen1.helpers.MovementHelper.*;
import static gen1.helpers.TerrainHelper.getOptimalLocation;

public class AttackHelper {

    private static final double ATTACK_THRESHOLD_RATIO = 0.8;

    public static boolean shouldAttack() {
        RobotInfo[] nearby = rc.senseNearbyRobots(actionRadius);
        int hp = 0;
        for (RobotInfo ri: nearby) {
            if (ri.team != mTeam) {
                hp += ri.conviction;
            }
        }
        return hp/((rc.getConviction()-10)*rc.getEmpowerFactor(mTeam, 0)) > ATTACK_THRESHOLD_RATIO;
    }

    public static MapLocation checkNearby() {
        // check nearby
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (RobotInfo ri: nearby) {
            if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return ri.location;
            }
        }
        return null;
    }


    private static ArrayList<Direction> movesToVacant = null;
    /*
     * @return
     *      1. next direction to move to if enlightenment center detected nearby
     *      2. next direction to move to if enlightenment center detected in the grid
     *      3. next random direction to move to
     *
     */

    public static Direction getNextDirection(MapLocation mLoc) throws GameActionException {
        // if movesToVacant is not empty return next move
        if (movesToVacant != null) {
            if (movesToVacant.isEmpty()) {
                movesToVacant = null;
            } else {
                Direction ret = movesToVacant.get(movesToVacant.size() - 1);
                // if blockage in path, re-compute path
                if (rc.canMove(ret)) {
                    movesToVacant.remove(movesToVacant.size() - 1);
                    return ret;
                }
            }
        }

        // check nearby
        MapLocation found = checkNearby();
        PassabilityGrid passability = new PassabilityGrid(mLoc, sensorRadius);
        if (found != null) {
            movesToVacant = getShortestRoute(mLoc, found, passability);
            if (movesToVacant != null) {
                return getNextDirection(mLoc);
            }
        }

        // check for info in grid flags
        MapLocation current = rc.getLocation();
        for (RobotInfo ri: rc.senseNearbyRobots()) {
            if (ri.team == mTeam && ri.type == RobotType.MUCKRAKER) {
                int flag = rc.getFlag(ri.getID());
                if (isPlaced(flag)) {
                    MapLocation ml = getCoordinatesFromFlag(flag);
                    if (ml != null) {
                        found = ml;
                        break;
                    }
                }
            }
        }
        if (found != null) {
            MapLocation ideal = getOptimalLocation(current, found, passability);
            movesToVacant = getShortestRoute(mLoc, ideal, passability);
            if (movesToVacant != null) {
                return getNextDirection(mLoc);
            }
        }

        return getRandomDirection();
    }

}
