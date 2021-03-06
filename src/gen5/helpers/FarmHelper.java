package gen5.helpers;

import battlecode.common.*;

import static gen5.RobotPlayer.*;
import static gen5.helpers.DefenseHelper.*;
import static gen5.helpers.MovementHelper.*;
import static gen5.util.Functions.convolveCircularly;

public class FarmHelper {
	private static Direction antiMuckDirection = null;
    private static int roundStarted = 0;

    public static Direction getAntiEcOrMuckDirection() {
        MapLocation current = rc.getLocation();
        byte[] occupied = new byte[8];
        int total = 0;
        boolean found = false;
        for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (ri.type == RobotType.MUCKRAKER || ri.type == RobotType.ENLIGHTENMENT_CENTER) {
            	found = true;
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
        if (maxRatio == 0 || !found) {
            return null;
        }
        return directions[(maxInd+4)%8];
    }

    public static Direction getAntiMuckDirection() {
        int[] blocked = new int[8];
        boolean found = false;
        for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                found = true;
                Direction toMuck = rc.getLocation().directionTo(ri.getLocation());
                ++blocked[toMuck.ordinal()];
                ++blocked[toMuck.rotateLeft().ordinal()];
                ++blocked[toMuck.rotateRight().ordinal()];
            }
        }

        if (!found) {
            if (antiMuckDirection != null) {
                if (roundNumber - roundStarted > 15) {
                    antiMuckDirection = null;
                    return null;
                }
                return antiMuckDirection;
            }
            return null;
        }

        antiMuckDirection = null;
        int minBlocked = 1000;
        for (Direction dir : directions) {
            int count = blocked[dir.ordinal()] + blocked[dir.rotateLeft().ordinal()] + blocked[dir.rotateRight().ordinal()];
            if (count < minBlocked) {
                minBlocked = count;
                antiMuckDirection = dir;
            }
        }

        roundStarted = roundNumber;
        return antiMuckDirection;
    }

    public static void tryRun(Direction dir, int innerRadius, int outerRadius) throws GameActionException {
    	MapLocation ml = rc.getLocation().add(dir);
		double straight = rc.canMove(dir) && !onWall(ml, innerRadius, outerRadius) ? rc.sensePassability(ml) : -1;
		ml = rc.getLocation().add(dir.rotateLeft());
    	double left = rc.canMove(dir.rotateLeft()) && !onWall(ml, innerRadius, outerRadius) ? rc.sensePassability(ml) : -1;
    	ml = rc.getLocation().add(dir.rotateRight());
		double right = rc.canMove(dir.rotateRight()) && !onWall(ml, innerRadius, outerRadius) ? rc.sensePassability(ml) : -1;

		if (straight > 0 && straight >= right && straight >= left) {
            rc.move(dir);
        } else if (left > 0 && left >= right && left >= straight) {
            rc.move(dir.rotateLeft());
        } else if (right > 0 && right >= straight && right >= left) {
            rc.move(dir.rotateRight());
        }
    }

    public static Direction directionToBuffMuck () {
        int threshold = roundNumber/30 + 10;
        Direction dir = null;
        for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (ri.type == RobotType.MUCKRAKER && ri.conviction >= threshold) {
                threshold = ri.conviction;
                dir = spawnerLocation.directionTo(ri.location);
            }
        }
        return dir;
    }
}
