package gen3;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import static gen3.RobotPlayer.*;

public strictfp class Slanderer {
	private static final int WALL_RADIUS_MIN = 18;
	private static final int WALL_RADIUS_MAX = 30;

	private static Direction toMove;
	private static double maxPassability;

    public static void move() throws GameActionException {
        if (!rc.isReady()) {
        	return;
        }

        if (spawnerLocation == null) {
            return;
        }
        int distanceFromSpawner = rc.getLocation().distanceSquaredTo(spawnerLocation);
        if (distanceFromSpawner >= WALL_RADIUS_MIN && distanceFromSpawner <= WALL_RADIUS_MAX) {
        	return;
        }

        toMove = null;
        maxPassability = 0d;

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        updateDirection(straight);
        Direction left = straight.rotateLeft();
        updateDirection(left);
        Direction right = straight.rotateRight();
        updateDirection(right);
        if (maxPassability > 0 && tryMove(toMove)) {
        	return;
        }

        for (int i = 0; i < 2; ++i) {
        	left = left.rotateLeft();
        	if (tryMove(left)) {
        		return;
        	}

        	right = right.rotateRight();
        	if (tryMove(right)) {
        		return;
        	}
        }

        left = left.rotateLeft();
        tryMove(left);
    }

    private static void updateDirection(Direction dir) throws GameActionException {
    	double passability = rc.sensePassability(rc.getLocation());
    	if (passability > maxPassability && rc.canMove(dir)) {
    		toMove = dir;
    		maxPassability = passability;
    	}
    }

    private static boolean tryMove(Direction dir) throws GameActionException {
    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }
}
