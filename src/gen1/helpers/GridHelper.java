package gen1.helpers;

import battlecode.common.*;

import java.util.*;

import static gen1.Muckraker.*;
import static gen1.RobotPlayer.*;
import static gen1.helpers.MovementHelper.*;


// muckraker info grid formation helper
public class GridHelper {
    public static final int MUCKRAKER_PLACED = 1;
    public static final int MUCKRAKER_GRID_WIDTH = 5;


    public static boolean isPlaced (int flag) {
        return (flag & MUCKRAKER_PLACED) > 0;
    }

    public static Direction getDirection(int flag) {
        return directions[(flag >> 5) % 8];
    }

    public static Direction getInitDirection(RobotInfo[] nearby) throws GameActionException {
        Direction ret = null;
        for (RobotInfo ri: nearby) {
            if (ri.team == mTeam && ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                MapLocation loc = ri.location;
                Direction dir = loc.directionTo(rc.getLocation());
                loc = multiply(loc, dir, MUCKRAKER_GRID_WIDTH);

                // check if to be placed at spot is vacant, otherwise return null
                if (rc.canSenseLocation(loc)) {
                    RobotInfo found = rc.senseRobotAtLocation(loc);
                    if (
                            found == null || found.team != mTeam ||
                                    found.type != RobotType.MUCKRAKER ||
                                    !isPlaced(rc.getFlag(found.getID()))
                    ) {
                        ret = ri.location.directionTo(rc.getLocation());
                    }
                }
            }
        }
        return ret;
    }

    /*
     * direction which needs to be stored in the flag after the bot is placed
     *
     * Direction finding priorities:
     *      1. corner muckraker (direct towards opposite direction) // TODO save statically for optimisation
     *      2. edge muckraker (forbid edge direction) // TODO save statically for optimisation
     *      3. avoid crowds by detecting ratio of occupied cells in each direction
     *      4. vacant position in adjacent grid (direct toward vacancy)
     *      5. no vacancies (select one random direction out of adjacent muckrakers)
     *
     */
    public static Direction getVacantDirection(RobotInfo[] nearby) throws GameActionException {
        //check for map edges/corners
        boolean[] edges = new boolean[8];
        MapLocation current = rc.getLocation();
        for (int i = 0; i < 8; i += 2) {
            MapLocation directed = multiply(current, directions[i], MUCKRAKER_GRID_WIDTH);
            edges[i] = !rc.onTheMap(directed);
            if (edges[i]) {
                edges[Math.floorMod(i-1, 8)] = edges[Math.floorMod(i+1, 8)] = true;
            }
        }

        //check if corner muckraker
        int free = 0;
        for (boolean b: edges) {
            free += b ? 0: 1;
        }
        // corner muckraker, send towards opposite direction
        if (free == 3) {
            for (int i = 1; i < 8; i+=2) {
                if (!edges[i]) {
                    return directions[i];
                }
            }
        }

        // find adjacent vacancies
        Direction vacancy = checkVacantSpot();
        if (vacancy != null) {
            return vacancy;
        }

        // TODO avoid crowding if any


        // select random direction out of adjacent muckrakers
        ArrayList<Direction> selected = new ArrayList<>();
        List<Direction> directionList = Arrays.asList(directions);
        for (RobotInfo ri: nearby) {
            int flag = rc.getFlag(ri.getID());
            if (ri.team == mTeam && ri.type == RobotType.MUCKRAKER && isPlaced(flag)) {
                Direction dir = getDirection(rc.getFlag(rc.getID()));
                int dirInd = directionList.indexOf(dir);
                // forbid edge direction
                if (!edges[dirInd]) {
                    selected.add(dir);
                }
            }
        }

        // forbid edge direction
        if (selected.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                if (!edges[i]) {
                    selected.add(directions[i]);
                }
            }
        }
        return (Direction) getRandom(selected.toArray());
    }

    private static Boolean isAdjacentTo(MapLocation mLocation, MapLocation placedLocation) {
        boolean yDif = Math.abs(placedLocation.y - mLocation.y) == MUCKRAKER_GRID_WIDTH,
                xDif = Math.abs(placedLocation.x - mLocation.x) == MUCKRAKER_GRID_WIDTH;
        if (placedLocation.x == mLocation.x) {
            return yDif;
        } else if (placedLocation.y == mLocation.y) {
            return xDif;
        } else {
            return yDif && xDif;
        }
    }

    private static Boolean isGridOverlapping(
            MapLocation mapLocation,
            ArrayList<MapLocation> placedLocations
    ) throws GameActionException {
        boolean overlapping = false;
        for (int i = 0; i < placedLocations.size(); i++) {
            if (!isAdjacentTo(mapLocation, placedLocations.get(i))) {
                for (int j = i+1; j < placedLocations.size(); j++) {
                    MapLocation a = placedLocations.get(i), b = placedLocations.get(j);
                    if (isAdjacentTo(a, b)) {
                        boolean first = true, second = true;
                        a = multiply(a, a.directionTo(mapLocation), MUCKRAKER_GRID_WIDTH);
                        b = multiply(b, b.directionTo(mapLocation), MUCKRAKER_GRID_WIDTH);
                        if (rc.canSenseLocation(a)) {
                            RobotInfo ri = rc.senseRobotAtLocation(a);
                            first = ri != null && ri.team == mTeam && isPlaced(rc.getFlag(ri.getID()));
                        }
                        if (rc.canSenseLocation(b)) {
                            RobotInfo ri = rc.senseRobotAtLocation(b);
                            second = ri != null && ri.team == mTeam && isPlaced(rc.getFlag(ri.getID()));
                        }
                        overlapping = first && second;
                        break;
                    }
                }
                if (overlapping) {
                    // tweak spawn enlightenment center coordinates to avoid wanderers deep in other grid
                    gridReferenceLocation = placedLocations.get(i);
                    break;
                }
            }
        }
        return overlapping;
    }


    // check if current position is valid for grid formation
    public static Boolean formsGrid (RobotInfo[] afterMoveNearby) throws GameActionException {
        MapLocation mapLocation = rc.getLocation();
        boolean validPos = false;
        ArrayList<MapLocation> placedLocations = new ArrayList<>();
        for (RobotInfo ri: afterMoveNearby) {
            int flag = rc.getFlag(ri.getID());
            if (ri.team == mTeam &&
                    (ri.type == RobotType.MUCKRAKER && isPlaced(flag) || ri.type == RobotType.ENLIGHTENMENT_CENTER)
            ) {
                if (isAdjacentTo(mapLocation, ri.location)) {
                    validPos = true;
                }
                placedLocations.add(ri.location);
            }
        }

        // invalid, return false
        if (!validPos) return false;

        // valid, check for overlapping grids
        return !isGridOverlapping(mapLocation, placedLocations);
    }

    // check for vacant grid spot in the sensor radius
    public static Direction checkVacantSpot() throws GameActionException {
        MapLocation mLoc = rc.getLocation();
        int sx = gridReferenceLocation.x, sy = gridReferenceLocation.y,
                mx = mLoc.x, my = mLoc.y;

        int modX = Math.floorMod(sx-mx, MUCKRAKER_GRID_WIDTH),
                modY = Math.floorMod(sy-my, MUCKRAKER_GRID_WIDTH),
                remX = (sx-mx) % MUCKRAKER_GRID_WIDTH,
                remY = (sy-my) % MUCKRAKER_GRID_WIDTH;
        Direction selected = null;

        MapLocation north, east, south, west;

        // placed muckraker condition
        if (modX == 0 && modY == 0) {
            north = new MapLocation(mx, my + MUCKRAKER_GRID_WIDTH);
            east = new MapLocation(mx + MUCKRAKER_GRID_WIDTH, my);
            south = new MapLocation(mx, my - MUCKRAKER_GRID_WIDTH);
            west = new MapLocation(mx - MUCKRAKER_GRID_WIDTH, my);
        } else { // muckraker not placed
            north = new MapLocation(mx + remX, my + modY);
            east = new MapLocation(mx + modX, my + remY);
            south = new MapLocation(mx + remX, my - MUCKRAKER_GRID_WIDTH + modY);
            west = new MapLocation(mx - MUCKRAKER_GRID_WIDTH + modX, my + remY);
        }

        //check in all 4 directions
        MapLocation[] possible = {north, east, south, west};
        for (MapLocation mp: possible) {
            if (rc.canSenseLocation(mp) && !rc.isLocationOccupied(mp)) {
                selected = mLoc.directionTo(mp);
            }
        }
        return selected;
    }
}
