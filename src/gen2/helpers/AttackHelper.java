package gen2.helpers;

import battlecode.common.*;

import gen2.util.*;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.Politician.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.helpers.TerrainHelper.getOptimalLocation;

public class AttackHelper {

    private static final double ATTACK_THRESHOLD_RATIO = 0.9;
    private static final double ATTACK_AFTER_ROUNDS = 4;

    private static final HashMap<MapLocation, Integer> roundsNotAttackedEC = new HashMap<>();

    // move pols away if lots of pol crowding within action radius
    public static Direction shouldBackOff () {
        RobotInfo[] nearby = rc.senseNearbyRobots();
        boolean nearbyEc = false;
        for (RobotInfo ri: nearby) {
            if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                nearbyEc = true;
                break;
            }
        }
        if (!nearbyEc) {
            return null;
        }
        MapLocation sum = rc.getLocation(), curr = sum;
        for (RobotInfo ri: nearby) {
            if (ri.type == RobotType.POLITICIAN && ri.getConviction() > rc.getConviction()) {
                sum = sum.translate(ri.getLocation().x - curr.x, ri.getLocation().y - curr.y);
            }
        }
        if (sum.equals(curr)) {
            return null;
        }
        return sum.directionTo(curr);
    }

    public static boolean targetAlreadyCaptured(MapLocation target) throws GameActionException {
        if (target.isWithinDistanceSquared(rc.getLocation(), sensorRadius)) {
            RobotInfo ri = rc.senseRobotAtLocation(target);
            if (ri != null) {
                return ri.team == mTeam;
            }
        }
        return false;
    }

    public static boolean shouldAttack() {
        RobotInfo[] nearby = rc.senseNearbyRobots(actionRadius);
        if (nearby.length == 0) {
            return false;
        }
        int damage = (int) (rc.getConviction()*rc.getEmpowerFactor(mTeam, 0)-10),
                each = damage/nearby.length, done = 0;
        MapLocation detectedEC = null;
        for (RobotInfo ri: nearby) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                done += each;
                if (ri.team != mTeam) {
                    detectedEC = ri.location;
                }
            } else if (ri.team != mTeam) {
                done += Math.min(ri.conviction, each);
            } else  {
                done += Math.min(ri.influence-ri.conviction, each);
            }
        }
        boolean attacking = done/(double) damage > ATTACK_THRESHOLD_RATIO;
        if (attacking) return true;
        if (detectedEC != null) {
            int got = roundsNotAttackedEC.getOrDefault(detectedEC, 0) + 1;
            if (got > ATTACK_AFTER_ROUNDS) {
                return true;
            }
            roundsNotAttackedEC.put(detectedEC, got);
        }
        return false;
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
        if (isAttackType || ec == null) {
            //TODO WALL TYPE POLITICIAN
            return getRandomDirection();
        }
        if (mLoc.isAdjacentTo(ec)) {
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

        if (ideal != null) {
            movesToVacant = getShortestRoute(mLoc, ideal, passability);
            if (movesToVacant != null) {
                return getNextDirection(mLoc, ec);
            }
        }

        return mLoc.directionTo(ec);
    }
}
