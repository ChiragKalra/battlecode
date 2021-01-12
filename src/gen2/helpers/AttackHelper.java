package gen2.helpers;

import battlecode.common.*;

import gen2.util.Pair;
import gen2.util.PassabilityGrid;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.helpers.TerrainHelper.getOptimalLocation;

public class AttackHelper {

    private static final double ATTACK_THRESHOLD_RATIO = 4;

    public static boolean shouldAttack(boolean attackType) {
        int hp = 0;
        for (RobotInfo ri: rc.senseNearbyRobots(2)) {
            if (ri.team != mTeam) {
                if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    return true;
                }
                hp += ri.conviction;
            }
        }
        if (attackType) return false;
        double hmm = hp/(rc.getConviction()*rc.getEmpowerFactor(mTeam, 0)-10);
        return hmm > ATTACK_THRESHOLD_RATIO;
    }

    public static Pair<MapLocation, Integer> getNearbyEnemyEC() {
        // check nearby
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (RobotInfo ri: nearby) {
            if (ri.team == enemyTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return new Pair<>(ri.location, ri.conviction);
            }
        }
        return null;
    }


    public static Pair<MapLocation, Integer> getNearbyNeutralEC() {
        // check nearby
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (RobotInfo ri: nearby) {
            if (ri.team == Team.NEUTRAL && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return new Pair<>(ri.location, ri.conviction);
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

    public static Direction getNextDirection(MapLocation mLoc, MapLocation ec) throws GameActionException {
        if (ec == null) {
            return getRandomDirection();
        }

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

        PassabilityGrid passability = new PassabilityGrid(mLoc, sensorRadius);
        MapLocation ideal = getOptimalLocation(mLoc, ec, passability);
        movesToVacant = getShortestRoute(mLoc, ideal, passability);
        if (movesToVacant != null) {
            return getNextDirection(mLoc, ec);
        }

        return getRandomDirection();
    }
}
