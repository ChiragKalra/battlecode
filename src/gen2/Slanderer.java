package gen2;

import battlecode.common.*;
import gen2.helpers.MovementHelper;
import gen2.util.PassabilityGrid;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.flags.MuckrakerFlag.*;

public strictfp class Slanderer {
	private static ArrayList<Direction> movesToVacant = null;
    private static MapLocation muckrakerLocation = null;
    private static int muckrakerID = -1;

    // TODO: run away from enemy muckrakers and politicians
    public static void move() throws GameActionException {
        MovementHelper.tryMove(getRandomDirection(), Precision.MIN);
/*
    	if (tryMoveShortestPath()) {
    		return;
    	}

    	// check if there's a muckraker in the sensing radius with a vacant adjacent location
    	boolean availableMuckraker = false;
        muckrakerID = -1;
        muckrakerLocation = new MapLocation(-1, -1);
    	MapLocation vacantLocation = new MapLocation(-1, -1);
    	int distanceToLocation = 1000;

    	for (RobotInfo robot : rc.senseNearbyRobots()) {
    		if (robot.getTeam() == mTeam && robot.getType() == RobotType.MUCKRAKER) {
    			// if there's a muckraker in an adjacent cell
                // TODO: muckraker has multiple adjacent slanderers
    			if (rc.getLocation().isAdjacentTo(robot.getLocation())) {
    				return;
    			}

                int muckrakerFlag = rc.getFlag(robot.getID());
                // a slanderer already present next to muckraker
                if (isAdjacentToSlanderer(muckrakerFlag)) {
                    continue;
                }

    			MapLocation ml = getAdjacentVacantLocation(robot.getLocation());
    			if (ml == null) {
    				continue;
    			}

    			availableMuckraker = true;
    			int distance = rc.getLocation().distanceSquaredTo(ml);
    			if (distance < distanceToLocation) {
    				muckrakerID = robot.getID();
                    muckrakerLocation = robot.getLocation();
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
        // TODO: use anti-crowding
        
        PassabilityGrid passability = new PassabilityGrid(rc.getLocation(), sensorRadius);
        movesToVacant = getShortestRoute(rc.getLocation(), vacantLocation, passability);
        if (movesToVacant == null) {
        	tryMove(getRandomDirection());
        } else {
        	tryMoveShortestPath();
        }*/
    }

    private static boolean tryMoveShortestPath() throws GameActionException {
    	if (movesToVacant == null) {
    		return false;
    	}
        if (movesToVacant.isEmpty()) {
            movesToVacant = null;
            return false;
        }
        if (muckrakerID == -1 || muckrakerLocation == null) {
            movesToVacant = null;
            return false;
        }

        Direction dir = movesToVacant.get(movesToVacant.size() - 1);
        if (rc.canMove(dir)) {
            if (rc.canGetFlag(muckrakerID)) {
                int muckrakerFlag = rc.getFlag(muckrakerID);
                if (isAdjacentToSlanderer(muckrakerFlag)) {
                    movesToVacant = null;
                    return false;
                }
            }

            // TODO: check all 8 adjacent cells of muckraker

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
    		if (rc.getLocation().isWithinDistanceSquared(adjacentLocation, sensorRadius) && rc.onTheMap(adjacentLocation)) {
    			if (!rc.isLocationOccupied(adjacentLocation)) {
        			foundLocation = true;
        			int distance = rc.getLocation().distanceSquaredTo(adjacentLocation);
        			if (distance < distanceToLocation) {
        				distanceToLocation = distance;
        				vacantLocation = adjacentLocation;
                    }
                } else {
                    RobotInfo robot = rc.senseRobotAtLocation(adjacentLocation);
                    // a slanderer already present next to muckraker
                    if (robot.getTeam() == mTeam && robot.getType() == RobotType.SLANDERER) {
                        return null;
                    }
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
