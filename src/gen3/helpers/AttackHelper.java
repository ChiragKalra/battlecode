package gen3.helpers;

import battlecode.common.*;
import gen3.flags.MuckrakerFlag;
import gen3.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

import static gen3.RobotPlayer.*;
import static gen3.flags.MuckrakerFlag.getHpFromFlag;
import static gen3.flags.MuckrakerFlag.isBroadcastingEC;
import static gen3.helpers.MovementHelper.*;

public class AttackHelper {

    private static final double ATTACK_THRESHOLD_RATIO = 0.8;
    private static final double ATTACK_AFTER_ROUNDS = 5;

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


    private static final HashSet<MapLocation> captured = new HashSet<>();
    public static boolean targetAlreadyCaptured(MapLocation target) throws GameActionException {
        if (captured.contains(target)) {
            return true;
        }
        if (target.isWithinDistanceSquared(rc.getLocation(), sensorRadius)) {
            RobotInfo ri = rc.senseRobotAtLocation(target);
            if (ri != null) {
                if (ri.team == mTeam) {
                    captured.add(target);
                    return true;
                }
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

    /*
     * @return
     *      1. next direction to move to if enlightenment center detected nearby
     *      2. next direction to move to if enlightenment center detected in the grid
     *      3. next random direction to move to
     *
     */

    public static Direction getNextDirection(MapLocation attackLocation) throws GameActionException {
        MapLocation mLoc = rc.getLocation();
        if (attackLocation == null) {
            for (RobotInfo ri: rc.senseNearbyRobots()) {
                if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    attackLocation = ri.location;
                    return mLoc.directionTo(ri.location);
                }
            }
        } else if (mLoc.isAdjacentTo(attackLocation)) {
            Direction anti = getAntiCrowdingDirection(mLoc);
            if (anti != null) {
                return getAntiCrowdingDirection(mLoc);
            }
        }
        return getRandomDirection();
    }

    public static Pair<MapLocation, Integer> checkForAttackCoordinates() throws GameActionException {
        Pair<MapLocation, Integer> selected = null;
        //check in all 4 directions
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                int flag = rc.getFlag(ri.getID());
                if (isBroadcastingEC(flag)) {
                    int hp = getHpFromFlag(flag);
                    if (selected == null || hp < selected.value) {
                        selected = new Pair<>(MuckrakerFlag.getAbsLocFromFlag(flag, ri.location), hp);
                    }
                }
            }
        }
        return selected;
    }
}
