package gen2;

import battlecode.common.*;
import gen2.util.PassabilityGrid;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;

public strictfp class Slanderer {
	private static ArrayList<Direction> movesToVacant = null;

    // TODO: run away from enemy muckrakers and politicians
    public static void move() throws GameActionException {
    	if (tryMoveShortestPath()) {
    		return;
    	}

    	// check if there's a muckraker in the sensing radius with a vacant adjacent location
    	boolean availableMuckraker = false;
    	// TODO: remove muckrakerLocation if not needed
    	MapLocation muckrakerLocation = new MapLocation(-1, -1);
    	MapLocation vacantLocation = new MapLocation(-1, -1);
    	int distanceToLocation = 1000;

    	for (RobotInfo ri : rc.senseNearbyRobots()) {
    		if (ri.getTeam() == mTeam && ri.getType() == RobotType.MUCKRAKER) {
    			// if there's a muckraker in an adjacent cell
    			if (rc.getLocation().isAdjacentTo(ri.getLocation())) {
    				return;
    			}

    			MapLocation ml = getAdjacentVacantLocation(ri.getLocation());
    			if (ml == null) {
    				continue;
    			}

    			availableMuckraker = true;
    			int distance = rc.getLocation().distanceSquaredTo(ml);
    			if (distance < distanceToLocation) {
    				muckrakerLocation = ri.getLocation();
    				vacantLocation = ml;
    				distanceToLocation = distance;
    			}
    		}
    	}

    	if (!availableMuckraker) {
        	tryMove(getRandomDirection());
        	return;
        }

        // TODO: get a list of available muckrakers and sort by distance, iterate and check if there's a path
        // TODO: move code to FarmHelper.java
        // if no available path tryMove()
        
        PassabilityGrid passability = new PassabilityGrid(rc.getLocation(), sensorRadius);
        movesToVacant = getShortestRoute(rc.getLocation(), vacantLocation, passability);
        if (movesToVacant == null) {
        	tryMove(getRandomDirection());
        } else {
        	tryMoveShortestPath();
        }
    }

    private static boolean tryMoveShortestPath() throws GameActionException {
    	if (movesToVacant == null) {
    		return false;
    	}
        if (movesToVacant.isEmpty()) {
            movesToVacant = null;
            return false;
        }

        Direction dir = movesToVacant.get(movesToVacant.size() - 1);
        if (rc.canMove(dir)) {
            movesToVacant.remove(movesToVacant.size() - 1);
            rc.move(dir);
            return true;
        }

        movesToVacant = null;
        return false;
    }

    private static MapLocation getAdjacentVacantLocation(MapLocation ml) throws GameActionException {
    	boolean foundLocation = false;
    	int distanceToLocation = 1000;
    	MapLocation vacantLocation = new MapLocation(-1, -1);

    	for (Direction dir : Direction.allDirections()) {
    		MapLocation adjacentLocation = ml.add(dir);
    		if (rc.getLocation().isWithinDistanceSquared(adjacentLocation, sensorRadius) && rc.onTheMap(adjacentLocation)
    			&& !rc.isLocationOccupied(adjacentLocation)) {
    			foundLocation = true;
    			int distance = rc.getLocation().distanceSquaredTo(adjacentLocation);
    			if (distance < distanceToLocation) {
    				distanceToLocation = distance;
    				vacantLocation = adjacentLocation;
    			}
    		}
    	}

    	return foundLocation ? vacantLocation : null;
    }

	private static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
