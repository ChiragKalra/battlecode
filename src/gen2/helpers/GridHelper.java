package gen2.helpers;

import battlecode.common.*;
import gen2.util.Pair;
import gen2.util.PassabilityGrid;

import java.util.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.flags.MuckrakerFlag.*;
import static gen2.util.Functions.getRandom;


// muckraker info grid formation helper
public class GridHelper {
    public static final int MUCKRAKER_GRID_WIDTH = 5;
    public static final int AVOID_EC_RADIUS_SQUARED = 25;

    private static Direction getAdjacentVacant(MapLocation current) throws GameActionException {
        int mx = current.x, my = current.y;
        MapLocation north = new MapLocation(mx, my + MUCKRAKER_GRID_WIDTH),
            east = new MapLocation(mx + MUCKRAKER_GRID_WIDTH, my),
            south = new MapLocation(mx, my - MUCKRAKER_GRID_WIDTH),
            west = new MapLocation(mx - MUCKRAKER_GRID_WIDTH, my);

        Direction selected = null;

        //check in all 4 directions
        MapLocation[] possible = {north, east, south, west};
        for (MapLocation mp: possible) {
            if (rc.canSenseLocation(mp) && !rc.isLocationOccupied(mp) && isNotNearDetectedEC(mp)) {
                selected = current.directionTo(mp);
                break;
            }
        }
        return selected;
    }

    // select random direction out of adjacent muckrakers
    public static Direction getDirectionFromAdjacentFlags(MapLocation now) throws GameActionException {
        ArrayList<Direction> selected = new ArrayList<>();
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius, mTeam);
        for (RobotInfo ri: nearby) {
            int flag = rc.getFlag(ri.getID());
            if (ri.type == RobotType.MUCKRAKER && isPlaced(flag)) {
                Direction dir = getDirection(rc.getFlag(rc.getID()));
                if (dir != null) {
                    Direction sum = vectorAddition(dir, now.directionTo(ri.location));
                    selected.add(sum == Direction.CENTER ? dir : sum);
                }
            }
        }
        return selected.isEmpty() ? null : (Direction) getRandom(selected.toArray());
    }

    /*
     * direction which needs to be stored in the flag after the bot is placed
     *
     * Direction finding priorities:
     *      1. vacant position in adjacent grid (direct toward vacancy)
     *      2. avoid crowds by detecting ratio of occupied cells in each direction
     *              (should take care of map edges and corners since it considers cells outside the map as occupied)
     *      3. no vacancies (select one random direction out of adjacent muckrakers)
     *
     */
    public static Pair<Direction, Boolean> getGridDirectionForFlag() throws GameActionException {
        MapLocation current = rc.getLocation();

        // find adjacent vacancies
        Direction vacancy = getAdjacentVacant(current);
        if (vacancy != null) {
            return new Pair<>(vacancy, true);
        }

        // avoid crowding if any
        Direction antiCrowd = getAntiCrowdingDirection(current);
        if (antiCrowd != null) {
            return new Pair<>(antiCrowd, false);
        }

        Direction adj = getDirectionFromAdjacentFlags(current);

        if (adj == null) {
            return null;
        }

        // select random direction out of adjacent muckrakers
        return new Pair<>(adj, false);
    }

    private static final HashSet<MapLocation> nearestECs = new HashSet<>();
    private static boolean isNotNearDetectedEC(MapLocation ml) {
        for (MapLocation loc: nearestECs) {
            if (loc.isWithinDistanceSquared(ml, AVOID_EC_RADIUS_SQUARED)) {
                return false;
            }
        }
        return true;
    }

    // check if current position is valid for grid formation
    public static Boolean formsGrid () {
        MapLocation mapLocation = rc.getLocation();

        // direct away from ECs to not absorb damage by politicians
        for (RobotInfo ri : rc.senseNearbyRobots(AVOID_EC_RADIUS_SQUARED)) {
            if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                nearestECs.add(ri.location);
                return false;
            }
        }

        return mapLocation.x % MUCKRAKER_GRID_WIDTH == 0 && mapLocation.y % MUCKRAKER_GRID_WIDTH == 0;
    }

    // check for vacant grid spot in the sensor radius
    private static MapLocation checkVacantSpot(MapLocation mLoc) throws GameActionException {
        int mx = mLoc.x, my = mLoc.y;

        int modX = Math.floorMod(-mx, MUCKRAKER_GRID_WIDTH),
                modY = Math.floorMod(-my, MUCKRAKER_GRID_WIDTH),
                remX = (-mx) % MUCKRAKER_GRID_WIDTH,
                remY = (-my) % MUCKRAKER_GRID_WIDTH;

        MapLocation north = new MapLocation(mx + remX, my + modY),
                east = new MapLocation(mx + modX, my + remY),
                south = new MapLocation(mx + remX, my - MUCKRAKER_GRID_WIDTH + modY),
                west = new MapLocation(mx - MUCKRAKER_GRID_WIDTH + modX, my + remY);

        //check in all 4 directions
        MapLocation[] possible = {north, east, south, west};
        for (MapLocation mp: possible) {
            if (rc.canSenseLocation(mp) && !rc.isLocationOccupied(mp) && isNotNearDetectedEC(mp)) {
                return mp;
            }
        }
        return null;
    }


    private static ArrayList<Direction> movesToVacant = null;

    /*
     * @return
     *      1. next direction to move to if vacancy detected nearby
     *      2. next direction to move to if directed by other muckrakers
     *      3. next random direction to move to
     *
     */
    public static Direction getNextDirection(MapLocation mLoc) throws GameActionException {
        // if movesToVacant is not empty return next move
        if (movesToVacant != null) {
            if (movesToVacant.isEmpty()) {
                movesToVacant = null;
            } else {
                Direction ret = movesToVacant.get(movesToVacant.size() - 1);
                // if blockage in path, re-compute path
                if (rc.canMove(ret)) {
                    movesToVacant.remove(movesToVacant.size() - 1);
                    return ret;
                }
            }
        }

        // direct away from ECs to not absorb damage by pols
        for (RobotInfo ri : rc.senseNearbyRobots(AVOID_EC_RADIUS_SQUARED)) {
            if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                return ri.location.directionTo(rc.getLocation());
            }
        }

        PassabilityGrid passability = new PassabilityGrid(mLoc, sensorRadius);

        MapLocation vacantSpot = checkVacantSpot(mLoc);
        if (vacantSpot != null) {
            movesToVacant = getShortestRoute(mLoc, vacantSpot, passability);
            if (movesToVacant != null) {
                return getNextDirection(mLoc);
            }
        }

        ArrayList<Direction> selected = new ArrayList<>();
        RobotInfo[] fellow = rc.senseNearbyRobots(sensorRadius, mTeam);
        for (RobotInfo ri : fellow) {
            if (ri.type == RobotType.MUCKRAKER) {
                try {
                    int flag = rc.getFlag(ri.getID());
                    if (isPlaced(flag)) {
                        Direction dir = getDirection(flag);
                        if (dir != null) {
                            selected.add(dir);
                        }
                    }
                } catch (GameActionException ignored) { }
            }
        }

        /*
        TODO : REPLACE
        vacantSpot = getOptimalLocationInDirection(mLoc, decided, passability);
        if (vacantSpot != null) {
            movesToVacant = getShortestRoute(mLoc, vacantSpot, passability);
            if (movesToVacant != null) {
                return getNextDirection(mLoc);
            }
        }
        */
        return selected.isEmpty() ? getRandomDirection() : (Direction) getRandom(selected.toArray());
    }
}
