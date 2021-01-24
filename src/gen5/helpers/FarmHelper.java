package gen5.helpers;

import battlecode.common.*;

import static gen5.RobotPlayer.*;
import static gen5.flags.EnlightenmentCenterFlag.getRadius;
import static gen5.helpers.DefenseHelper.*;
import static gen5.helpers.MovementHelper.directions;

public class FarmHelper {
	private static Direction antiMuckDirection = null;
    private static int roundsRunning = 0;

    public static Direction getAntiMuckDirection() throws GameActionException {
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
                if (roundsRunning >= 6) {
                    antiMuckDirection = null;
                    roundsRunning = 0;
                    return null;
                }
                ++roundsRunning;
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

        roundsRunning = 1;
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
}