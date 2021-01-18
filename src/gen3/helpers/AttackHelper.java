package gen3.helpers;

import battlecode.common.*;
import gen3.flags.MuckrakerFlag;
import gen3.util.Pair;

import java.util.HashMap;

import static gen3.RobotPlayer.*;
import static gen3.flags.MuckrakerFlag.getHpFromFlag;
import static gen3.flags.MuckrakerFlag.isBroadcastingEC;
import static gen3.helpers.MovementHelper.*;

public class AttackHelper {

    private static final double EMP_ATTACK_THRESHOLD_RATIO = 0.9;
    private static final double EMP_AFTER_ROUNDS = 5;

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

    public static boolean shouldAttackOffensive() {
        RobotInfo[] nearby = rc.senseNearbyRobots(1);
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
        boolean attacking = done/(double) damage > EMP_ATTACK_THRESHOLD_RATIO;
        if (attacking) return true;
        if (detectedEC != null) {
            int got = roundsNotAttackedEC.getOrDefault(detectedEC, 0) + 1;
            if (got > EMP_AFTER_ROUNDS) {
                return true;
            }
            roundsNotAttackedEC.put(detectedEC, got);
        }
        return false;
    }

    public static int shouldAttackDefensive () {
        RobotInfo[] nearby = rc.senseNearbyRobots(actionRadius, enemyTeam);
        int[] check = {1, 2, 4, 5, 8, 9, 10};
        MapLocation location = rc.getLocation();

        for (int rad : check) {
            for (RobotInfo ri : nearby) {
                if (location.isWithinDistanceSquared(ri.location, rad)
                        && ri.type == RobotType.MUCKRAKER) {
                    return rad;
                }
            }
        }
        return 0;
    }

    /*
     * @return
     *      1. next direction to move to if enlightenment center detected nearby
     *      2. next direction to move to if enlightenment center detected in the grid
     *      3. next random direction to move to
     *
     */

    public static Direction getNextDirection(MapLocation attackLocation) {
        MapLocation mLoc = rc.getLocation();
        if (attackLocation == null) {
            for (RobotInfo ri: rc.senseNearbyRobots()) {
                if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    return mLoc.directionTo(ri.location);
                }
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
