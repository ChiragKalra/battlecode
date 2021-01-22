package gen5.helpers;

import battlecode.common.*;
import gen5.flags.GridPoliticianFlag;
import gen5.util.Pair;

import java.util.HashMap;

import static gen5.RobotPlayer.*;
import static gen5.flags.GridPoliticianFlag.getHpFromFlag;
import static gen5.flags.GridPoliticianFlag.isBroadcastingEC;
import static gen5.helpers.MovementHelper.*;


public class AttackHelper {

    private static final double EMP_ATTACK_THRESHOLD_RATIO = 0.75;
    private static final double EMP_AFTER_ROUNDS = 4;

    private static final HashMap<MapLocation, Integer> roundsNotAttackedEC = new HashMap<>();

    public static boolean shouldSelfEmpower () {
        int distance = spawnerLocation == null ? 100000: rc.getLocation().distanceSquaredTo(spawnerLocation);
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (rc.getLocation().distanceSquaredTo(ri.location) < distance) {
                    spawnerLocation = ri.location;
                    enlightenmentCenterId = ri.getID();
                }
            }
        }

        if (spawnerLocation == null) {
            return false;
        }

        double dis = Math.sqrt(distance), factor = Math.min(rc.getEmpowerFactor(mTeam, (int)(dis*3)), 10000)-1;

        return factor*rc.getConviction() > 500;
    }


    public static int shouldAttackOffensive() {
        RobotInfo[] nearby = rc.senseNearbyRobots(1);
        if (nearby.length == 0) {
            return 0;
        }
        double empFac = rc.getEmpowerFactor(mTeam, 0), selfEmpFac = 1;
        if (empFac > 10000) {
            empFac = 10000;
        }
        int damage = (int) (rc.getConviction()*empFac-10),
                each = damage/nearby.length, done = 0;
        MapLocation detectedEC = null;
        for (RobotInfo ri: nearby) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                done += each;
                detectedEC = ri.location;
                if (empFac > 100) {
                    return actionRadius;
                }
                if (ri.team == mTeam) {
                    selfEmpFac = 1.1112;
                }
            } else if (ri.team != mTeam) {
                done += Math.min(ri.conviction, each);
            } else  {
                done += Math.min(ri.influence-ri.conviction+1, each);
            }
        }
        boolean attacking = done/(double) damage > EMP_ATTACK_THRESHOLD_RATIO*selfEmpFac;
        if (attacking) return 1;
        if (detectedEC != null) {
            int got = roundsNotAttackedEC.getOrDefault(detectedEC, 0) + 1;
            if (got > EMP_AFTER_ROUNDS) {
                return 1;
            }
            roundsNotAttackedEC.put(detectedEC, got);
        }
        return 0;
    }

    // returns (radius, is a muckraker adjacent)
    public static Pair<Integer, Boolean> shouldAttackDefensive () {
        /*
        TODO check if 3 layers on wall, dont attack
        if (spawnerLocation != null) {
            MapLocation now = rc.getLocation();
            Direction toSpawn = now.directionTo(spawnerLocation);
            for (int i = 0; i < 2; i++) {

            }
        }*/


        int[] check = {1, 2, 4, 5, 8, 9};

        double empFac = rc.getEmpowerFactor(mTeam, 0);
        if (empFac > 10000) {
            empFac = 10000;
        }
        
        Integer empRad = 0;
        int mostKills = 0, mostDamage = 0;
        Boolean muckrakerAdjacent = false;
        for (int rad : check) {
            RobotInfo[] nearby = rc.senseNearbyRobots(rad);
            if (nearby.length == 0) continue;
            int damage = (int) (rc.getConviction()*empFac-10),
                    each = damage/nearby.length, kills = 0, damageDone = 0;
            for (RobotInfo ri: nearby) {
                if (ri.team != mTeam && ri.type != RobotType.ENLIGHTENMENT_CENTER) {
                    if (ri.conviction <= rc.getConviction()*3) {
                        if (ri.conviction < each) {
                            kills++;
                            damageDone += ri.conviction + 1;
                        } else {
                            damageDone += each;
                        }
                    }

                    if (ri.type == RobotType.MUCKRAKER && rad <= 2) {
                        muckrakerAdjacent = true;
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
        return new Pair<>(empRad, muckrakerAdjacent);
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
            if (ri.type == RobotType.POLITICIAN) {
                int flag = rc.getFlag(ri.getID());
                if (isBroadcastingEC(flag)) {
                    int hp = getHpFromFlag(flag);
                    MapLocation loc = GridPoliticianFlag.getAbsLocFromFlag(flag, ri.location);
                    if (selected == null || hp < selected.value) {
                        selected = new Pair<>(loc, hp);
                    }
                }
            }
        }
        return selected;
    }
}
