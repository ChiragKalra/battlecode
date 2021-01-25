package gen5.helpers;

import battlecode.common.*;
import gen5.flags.GridPoliticianFlag;
import gen5.util.EcInfo;
import gen5.util.Vector;

import java.util.ArrayList;

import static gen5.RobotPlayer.*;
import static gen5.flags.GridPoliticianFlag.*;
import static gen5.helpers.MovementHelper.*;
import static gen5.util.Functions.getRandom;


// POLITICIAN info grid formation helper
public class GridHelper {
    public static final int GRID_WIDTH = 5;
    public static final int GRID_X = 1;
    public static final int GRID_Y = 3;
    public static final int ROUND_BROADCAST_CAPTURED = 13;

    private static Direction getAdjacentVacant (MapLocation current) throws GameActionException {
        int mx = current.x, my = current.y;
        MapLocation north = new MapLocation(mx, my + GRID_WIDTH),
            east = new MapLocation(mx + GRID_WIDTH, my),
            south = new MapLocation(mx, my - GRID_WIDTH),
            west = new MapLocation(mx - GRID_WIDTH, my);

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
    public static Direction getGridDirectionForFlag(RobotInfo[] fellow) throws GameActionException {
        MapLocation current = rc.getLocation();

        // find adjacent vacancies
        Direction vacancy = getAdjacentVacant(current);
        if (vacancy != null) {
            return vacancy;
        }

        // avoid crowding if any
        Direction antiCrowd = getAntiCrowdingDirection(current);
        if (antiCrowd != null) {
            return antiCrowd;
        }

        // select random direction out of adjacent muckrakers
        return getDirectionFromAdjacentFlags(current, fellow);
    }

    // check if current position is valid for grid formation
    public static Boolean formsGrid () {
        MapLocation mapLocation = rc.getLocation();

        return mapLocation.x % GRID_WIDTH == GRID_X &&
                mapLocation.y % GRID_WIDTH == GRID_Y;
    }

    // check for vacant grid spot in the sensor radius
    private static MapLocation checkVacantSpot(MapLocation mLoc) throws GameActionException {
        int mx = mLoc.x, my = mLoc.y;

        int modX = Math.floorMod(GRID_X -mx, GRID_WIDTH),
                modY = Math.floorMod(GRID_Y -my, GRID_WIDTH);

        MapLocation ne = new MapLocation(mx + modX, my + modY),
                se = new MapLocation(mx + modX, my + modY - GRID_WIDTH),
                sw = new MapLocation(mx + modX - GRID_WIDTH, my + modY - GRID_WIDTH),
                nw = new MapLocation(mx + modX - GRID_WIDTH, my + modY);

        //check in all 4 directions
        MapLocation[] possible = {ne, se, sw, nw};
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
    public static Direction getDirectionsToVacancy () throws GameActionException {
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
    private static Direction lastAdjFlag = null;
    private static int lastChange = 0;
    public static Direction getNextDirection(RobotInfo[] fellow) throws GameActionException {
        // direct away from ECs to not absorb damage by pols
        ArrayList<Direction> selected = new ArrayList<>();
        for (RobotInfo ri : fellow) {
            if (ri.type == RobotType.POLITICIAN && rc.canGetFlag(ri.getID())) {
                int flag = rc.getFlag(ri.getID());
                if (isPlaced(flag)) {
                    Direction dir = getDirection(flag);
                    if (dir != null) {
                        selected.add(dir);
                    }
                }
            }
        }
        if (lastChange == 0 || lastAdjFlag == null) {
            if (!selected.isEmpty()) {
                lastChange = ROUND_BROADCAST_CAPTURED;
                lastAdjFlag = selected.get(0);
                return selected.get(0);
            }
        } else {
            lastChange--;
            return lastAdjFlag;
        }
        return getRandomDirection();
    }

    // select random direction out of adjacent muckrakers
    public static Direction getDirectionFromAdjacentFlags(MapLocation now, RobotInfo[] nearby) throws GameActionException {
        ArrayList<Direction> selected = new ArrayList<>();
        for (RobotInfo ri: nearby) {
            int flag = rc.getFlag(ri.getID());
            if (ri.type == RobotType.POLITICIAN && isPlaced(flag)) {
                Direction dir = getDirection(rc.getFlag(rc.getID()));
                if (dir != null) {
                    Direction sum = vectorAddition(dir, now.directionTo(ri.location));
                    selected.add(sum == Direction.CENTER ? dir : sum);
                }
            }
        }
        return selected.isEmpty() ? null : (Direction) getRandom(selected.toArray());
    }

    private static int lastRoundSeen = -1;
    public static EcInfo getNearbyEC() {
        // check nearby
        MapLocation loc = rc.getLocation();
        RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius);
        MapLocation mEc = null;
        for (RobotInfo ri: nearby) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER || ri.type == RobotType.MUCKRAKER && ri.conviction > 150) {
                int x = ri.location.x >= loc.x ? 1 : 0, y = ri.location.y >= loc.y ? 1 : 0;
                mEc = new MapLocation(x, y);
                if (ri.team != mTeam) {
                    lastRoundSeen = roundNumber;
                    return new EcInfo(mEc, ri.conviction, ri.team == enemyTeam);
                }
            }
        }
        if (mEc != null && lastRoundSeen != -1 && Math.abs(lastRoundSeen-roundNumber) <= ROUND_BROADCAST_CAPTURED) {
            return new EcInfo(mEc, -HP_LOSS_RATIO, false);
        }
        return null;
    }

    private static final Vector<MapLocation> captured = new Vector<>(null, 100);
    private static MapLocation broadcastingCaptured = null;
    private static int roundsBroadcasted = 0;
    public static EcInfo getECFromAdjFlags() throws GameActionException {
        if (broadcastingCaptured != null) {
            roundsBroadcasted++;
            if (roundsBroadcasted > ROUND_BROADCAST_CAPTURED) {
                captured.add(broadcastingCaptured);
                broadcastingCaptured = null;
                roundsBroadcasted = 0;
            }
        }

        MapLocation current = rc.getLocation();

        EcInfo selected = null;

        //check in all 4 directions
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.POLITICIAN && rc.canGetFlag(ri.getID())) {
                int flag = rc.getFlag(ri.getID());
                if (isBroadcastingEC(flag)) {
                    MapLocation got = GridPoliticianFlag.getRelLocFromFlag(flag);
                    Direction dir = current.directionTo(ri.location);
                    got = got.add(dir);
                    int hp = getHpFromFlag(flag);
                    if (selected == null || hp < 0 || distanceSquared(got) < distanceSquared(selected.location)) {
                        if (!captured.has(got)) {
                            selected = new EcInfo(got, hp, isEnemyEc(flag));
                            if (hp <= 0 && broadcastingCaptured == null) {
                                broadcastingCaptured = got;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }

    private static int distanceSquared (MapLocation p) {
        return p.x*p.x + p.y*p.y;
    }
}
