package gen4.helpers;

import battlecode.common.*;
import gen4.flags.MuckrakerFlag;
import gen4.util.Pair;

import java.util.HashMap;

import static gen4.RobotPlayer.*;
import static gen4.flags.MuckrakerFlag.getHpFromFlag;
import static gen4.flags.MuckrakerFlag.isBroadcastingEC;
import static gen4.helpers.MovementHelper.*;


public class AttackHelper {

    private static final double EMP_ATTACK_THRESHOLD_RATIO = 0.9;
    private static final double EMP_AFTER_ROUNDS = 4;

    private static final HashMap<MapLocation, Integer> roundsNotAttackedEC = new HashMap<>();


    public static int shouldAttackOffensive() {
        RobotInfo[] nearby = rc.senseNearbyRobots(1);
        if (nearby.length == 0) {
            return 0;
        }
        double empFac = rc.getEmpowerFactor(mTeam, 0);
        if (empFac > 1000) {
            return actionRadius;
        }
        int damage = (int) (rc.getConviction()*empFac-10),
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
                done += Math.min(ri.influence-ri.conviction+1, each);
            }
        }
        boolean attacking = done/(double) damage > EMP_ATTACK_THRESHOLD_RATIO;
        if (attacking && !(detectedEC == null && nearby.length == 1)) return 1;
        if (detectedEC != null) {
            int got = roundsNotAttackedEC.getOrDefault(detectedEC, 0) + 1;
            if (got > EMP_AFTER_ROUNDS) {
                return 1;
            }
            roundsNotAttackedEC.put(detectedEC, got);
        }
        return 0;
    }

    public static int shouldAttackDefensive () {
        int[] check = {1, 2, 4, 5, 8, 9};

        double empFac = rc.getEmpowerFactor(mTeam, 0);
        if (empFac > 1000) {
            return actionRadius;
        }
        int empRad = 0, mostKills = 0, mostDamage = 0;
        for (int rad : check) {
            RobotInfo[] nearby = rc.senseNearbyRobots(rad);
            if (nearby.length == 0) continue;
            int damage = (int) (rc.getConviction()*empFac-10),
                    each = damage/nearby.length, kills = 0, damageDone = 0;
            for (RobotInfo ri: nearby) {
                if (ri.team != mTeam) {
                    if (ri.conviction < each) {
                        kills++;
                        damageDone += ri.conviction+1;
                    } else {
                        damageDone += each;
                    }
                }
            }
            if (kills > mostKills) {
                empRad = rad;
                mostKills = kills;
                mostDamage = damage;
            } else if (kills == mostKills && damageDone > mostDamage) {
                empRad = rad;
                mostKills = kills;
                mostDamage = damage;
            }
        }
        return empRad;
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
                    MapLocation loc = MuckrakerFlag.getAbsLocFromFlag(flag, ri.location);
                    if (selected == null || hp < selected.value) {
                        selected = new Pair<>(loc, hp);
                    }
                }
            }
        }
        return selected;
    }
}
