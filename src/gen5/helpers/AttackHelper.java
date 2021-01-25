package gen5.helpers;

import battlecode.common.*;
import gen5.flags.GridPoliticianFlag;
import gen5.util.EcInfo;
import gen5.util.Pair;
import gen5.util.SpawnType;

import java.util.HashMap;

import static gen5.RobotPlayer.*;
import static gen5.flags.GridPoliticianFlag.*;
import static gen5.helpers.MovementHelper.*;


public class AttackHelper {

    private static final double EMP_ATTACK_THRESHOLD_RATIO = 0.8;
    private static final double EMP_AFTER_ROUNDS = 6;

    private static final int[] check = {1, 2, 4, 5, 8, 9};

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
        int bestRad = 0, ecRad = 100;
        double bestDamage = 0, empFac = rc.getEmpowerFactor(mTeam, 0), selfEmpFac = 1;
        MapLocation detectedEC = null;
        for (int rad : check) {
            RobotInfo[] nearby = rc.senseNearbyRobots(rad);
            if (nearby.length == 0) {
                return 0;
            }
            if (empFac > 10000) {
                empFac = 10000;
            }
            int damage = (int) (rc.getConviction() * empFac - 10),
                    each = damage / nearby.length, done = 0;
            for (RobotInfo ri : nearby) {
                if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    done += each;
                    if (empFac > 100) {
                        return actionRadius;
                    }
                    if (ri.team == mTeam) {
                        selfEmpFac = 1.25;
                    } else {
                        detectedEC = ri.location;
                        ecRad = Math.min(rad, ecRad);
                    }
                } else if (ri.team != mTeam) {
                    done += Math.min(ri.conviction, each);
                } else {
                    done += Math.min(ri.influence - ri.conviction + 1, each);
                }
            }
            double ratio = done / (double) rc.getConviction();
            boolean attacking = ratio > EMP_ATTACK_THRESHOLD_RATIO * selfEmpFac;
            if (attacking && ratio > bestDamage) {
                bestDamage = ratio;
                bestRad = rad;
            }
            selfEmpFac = 1;
        }
        if (detectedEC != null) {
            int got = roundsNotAttackedEC.getOrDefault(detectedEC, 0) + 1;
            if (got > EMP_AFTER_ROUNDS) {
                return ecRad;
            }
            roundsNotAttackedEC.put(detectedEC, got);
        }
        return bestRad;
    }

    // returns (radius, is a muckraker adjacent)
    public static Pair<Integer, Boolean> shouldAttackDefensive () {
        double empFac = rc.getEmpowerFactor(mTeam, 0);
        if (empFac > 10000) {
            empFac = 10000;
        }
        
        int empRad = 0;
        int mostKills = 0, mostDamage = 0;
        boolean muckrakerAdjacent = false;
        for (int rad : check) {
            RobotInfo[] nearby = rc.senseNearbyRobots(rad);
            if (nearby.length == 0) continue;
            int damage = (int) (rc.getConviction()*empFac-10),
                    each = damage/nearby.length, kills = 0, damageDone = 0, mHp = rc.getConviction();
            for (RobotInfo ri: nearby) {
                if (
                        ri.team != mTeam && ri.type != RobotType.ENLIGHTENMENT_CENTER &&
                                (ri.conviction <= (mHp-10)*5 || rc.getInfluence() == SpawnType.DefensePolitician.maxHp)
                ) {
                    if (ri.conviction < each) {
                        kills++;
                        damageDone += ri.conviction + 1;
                    } else {
                        damageDone += each;
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

    private static Direction opEc = null;
    private static int moves = 0;
    public static Direction getNextDirection(MapLocation attackLocation) throws GameActionException {
        if (moves > 0 && opEc != null) {
            if (!rc.onTheMap(rc.getLocation().add(opEc))) {
                opEc = opEc.rotateLeft();
            }
            moves--;
            return opEc;
        }

        MapLocation mLoc = rc.getLocation();
        if (attackLocation == null) {
            for (RobotInfo ri: rc.senseNearbyRobots()) {
                if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                    if (ri.team != mTeam) {
                        return mLoc.directionTo(ri.location);
                    } else {
                        opEc = ri.location.directionTo(mLoc);
                        moves = 30;
                    }
                }
            }
        }
        Direction grid = GridHelper.getDirectionFromAdjacentFlags(mLoc);
        return grid != null ? grid : getRandomDirection();
    }

    public static EcInfo checkForAttackCoordinates() throws GameActionException {
        EcInfo selected = null;
        //check in all 4 directions
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.POLITICIAN) {
                int flag = rc.getFlag(ri.getID());
                if (isBroadcastingEC(flag)) {
                    int hp = getHpFromFlag(flag);
                    MapLocation loc = GridPoliticianFlag.getAbsLocFromFlag(flag, ri.location);
                    if (selected == null || hp < selected.hp) {
                        selected = new EcInfo(loc, hp, isEnemyEc(flag));
                    }
                }
            }
        }
        return selected;
    }
}
