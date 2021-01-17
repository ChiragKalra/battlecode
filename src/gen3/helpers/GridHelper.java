package gen3.helpers;

import battlecode.common.*;
import gen3.flags.MuckrakerFlag;
import gen3.util.Pair;

import java.util.ArrayList;

import static gen3.RobotPlayer.*;
import static gen3.flags.MuckrakerFlag.*;
import static gen3.helpers.MovementHelper.*;
import static gen3.util.Functions.getRandom;


// muckraker info grid formation helper
public class GridHelper {
    public static final int MUCKRAKER_GRID_WIDTH = 5;

    private static Direction getAdjacentVacant (MapLocation current) throws GameActionException {
        int mx = current.x, my = current.y;
        MapLocation north = new MapLocation(mx, my + MUCKRAKER_GRID_WIDTH),
            east = new MapLocation(mx + MUCKRAKER_GRID_WIDTH, my),
            south = new MapLocation(mx, my - MUCKRAKER_GRID_WIDTH),
            west = new MapLocation(mx - MUCKRAKER_GRID_WIDTH, my);

        Direction selected = null;

        //check in all 4 directions
        MapLocation[] possible = {north, east, south, west};
        for (MapLocation mp: possible) {
            if (rc.canSenseLocation(mp) && !rc.isLocationOccupied(mp)) {
                selected = current.directionTo(mp);
                break;
            }
        }
        return selected;
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

    // check if current position is valid for grid formation
    public static Boolean formsGrid () {
        MapLocation mapLocation = rc.getLocation();

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
            if (rc.canSenseLocation(mp) && !rc.isLocationOccupied(mp)) {
                return mp;
            }
        }
        return null;
    }

    /*
     * @return
     *      1. direction feeder to vacancy
     *      2. null if no vacancy nearby
     *
     */
    private static int explodedRadius = 0;
    public static Direction getDirectionsToVacancy () throws GameActionException {
        if (rc.getRoundNum() < 150 && explodedRadius < 4*5) {
            explodedRadius++;
            return spawnerLocation.directionTo(rc.getLocation());
        }

        MapLocation mLoc = rc.getLocation(), vacantSpot = checkVacantSpot(mLoc);
        if (vacantSpot != null) {
            return mLoc.directionTo(vacantSpot);
        }
        return null;
    }

    /*
     * @return
     *      1. next direction to move to if directed by other muckrakers
     *      2. next random direction to move to
     *
     */
    public static Direction getNextDirection() throws GameActionException {
        // direct away from ECs to not absorb damage by pols
        ArrayList<Direction> selected = new ArrayList<>();
        RobotInfo[] fellow = rc.senseNearbyRobots(sensorRadius, mTeam);
        for (RobotInfo ri : fellow) {
            if (ri.type == RobotType.MUCKRAKER && rc.canGetFlag(ri.getID())) {
                int flag = rc.getFlag(ri.getID());
                if (isPlaced(flag)) {
                    Direction dir = getDirection(flag);
                    if (dir != null) {
                        selected.add(dir);
                    }
                }
            }
        }
        return selected.isEmpty() ? getRandomDirection() : (Direction) getRandom(selected.toArray());
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

    public static Pair<Pair<Integer, Integer>, Integer> getNearbyEC() {
        // check nearby
        MapLocation loc = rc.getLocation();
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius);
        for (RobotInfo ri: nearby) {
            if (ri.team != mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                int x = ri.location.x >= loc.x ? 1 : -1, y = ri.location.y >= loc.y ? 1 : -1;
                return new Pair<>(new Pair<>(x, y), ri.conviction);
            }
        }
        return null;
    }

    public static Pair<Pair<Integer, Integer>, Integer> getECFromAdjFlags() throws GameActionException {
        MapLocation current = rc.getLocation();
        Pair<Pair<Integer, Integer>, Integer> selected = null;

        //check in all 4 directions
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                int flag = rc.getFlag(ri.getID());
                if (isBroadcastingEC(flag)) {
                    int hp = getHpFromFlag(flag);
                    if (hp >= 0 && (selected == null || hp < selected.value)) {
                        Pair<Integer, Integer> got = MuckrakerFlag.getRelLocFromFlag(flag);
                        Direction dir = current.directionTo(ri.location);
                        got.key += dir.dx;
                        got.value += dir.dx;
                        selected = new Pair<>(got, hp);
                    }
                }
            }
        }
        return selected;
    }
}
