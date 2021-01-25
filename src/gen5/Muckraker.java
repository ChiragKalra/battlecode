package gen5;

import battlecode.common.*;
import gen5.util.EcInfo;

import static gen5.RobotPlayer.*;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.MovementHelper.*;
import static gen5.util.Functions.convolveCircularly;

public strictfp class Muckraker {

    private static Direction getAntiCrowdingDirection(MapLocation current) {
        byte[] occupied = new byte[8];
        int total = 0;
        for (RobotInfo ri : rc.senseNearbyRobots(actionRadius, mTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                occupied[directionList.indexOf(current.directionTo(ri.location))]++;
                total++;
            }
        }

        double[] ratios = new double[8], filter = {.2, .6, .2};
        for (int i = 0; i < 8; i++) {
            ratios[i] = occupied[i] / (float) total;
        }
        ratios = convolveCircularly(ratios, filter);

        int maxInd = -1;
        double maxRatio = 0;
        for (int i = 0; i < 8; i++) {
            if (ratios[i] > maxRatio) {
                maxRatio = ratios[i];
                maxInd = i;
            }
        }
        if (maxRatio == 0) {
            return null;
        }
        return directions[(maxInd+4)%8];
    }

    private static MapLocation getOptimalLocationToExpose () {
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius, enemyTeam);
        for (RobotInfo ri: nearby) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return ri.location;
            }
        }
        return null;
    }

    private static Direction con = null;
    private static int rounds = 0;
    public static Direction getNextDirection() throws GameActionException {
        if (con == null || rounds == 0) {
            con = getRandomDirection();
            rounds = 100;
        }
        if (!rc.onTheMap(rc.getLocation().add(con))) {
            con = con.rotateLeft();
        }
        rounds--;
        return con;
    }


    public static void move() throws GameActionException {
        MapLocation locToExpose;
        // movement
        locToExpose = getOptimalLocationToExpose();

        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (robot.type.canBeExposed()) {
                if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius)) {
                    // expose the slanderer
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                        return;
                    }
                } else {
                    locToExpose = robot.location;
                }
            }
        }

        Direction anti = getAntiCrowdingDirection(rc.getLocation());
        if (anti != null) {
            tryMove(anti, false);
        } else if (locToExpose != null) {
            goTo(locToExpose);
        } else {
            EcInfo got = checkForAttackCoordinates();
            if (got != null && got.hp >= 0 && got.enemy) {
                goTo(got.location);
            } else {
                tryMove(getNextDirection(), false);
            }
        }
    }

    public static void init() { }

}
