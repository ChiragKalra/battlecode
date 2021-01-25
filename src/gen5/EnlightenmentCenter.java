package gen5;

import battlecode.common.*;
import gen5.helpers.GridHelper;
import gen5.util.EcInfo;
import gen5.util.SpawnType;

import java.util.Arrays;

import static gen5.RobotPlayer.*;
import static gen5.helpers.AttackHelper.checkForAttackCoordinates;
import static gen5.helpers.MovementHelper.directionList;
import static gen5.helpers.MovementHelper.directions;
import static gen5.helpers.SpawnHelper.*;
import static gen5.util.SpawnType.getOptimalType;


public strictfp class EnlightenmentCenter {

    public static int currentRadius = 4;

    public static double RATIO_BID = 0.08;
    public static double RATIO_UNITS = 0.02;

    public static int roundCaptured = 1;
    public static int directionsBlocked = 0;
    public static boolean[] edgeAtDirection = new boolean[4], slandererDirection = new boolean[8];
    public static int[] edgeDistance = new int[4];
    public static Direction shiftedTunnel = Direction.CENTER;
    public static EcInfo targetEC;


    private static boolean spawnOptimal() throws GameActionException {
        boolean spawned = false;
        SpawnType got = getOptimalType(targetEC);
        if (got != null) {
            switch (got) {
                case BuffMuckraker:
                    spawned = spawnMuckraker(targetEC.location, true);
                    break;
                case AttackPolitician:
                    spawned = spawnAttackPolitician(targetEC.location, targetEC.hp);
                    break;
                case GridPolitician:
                    spawned = spawnGridPolitician();
                    break;
                case DefensePolitician:
                    spawned = spawnDefencePolitician();
                    break;
                case FillerMuckraker:
                    spawned = spawnFillerMuckraker();
                    break;
                case Slanderer:
                    spawned = spawnSlanderer();
                    break;
                case Muckraker:
                    spawned = spawnMuckraker(targetEC == null ? null : targetEC.location, false);
                    break;
            }
            if (!spawned) {
                spawned = spawnDefencePolitician();
            }
        }
        return spawned;
    }


    public static void init() throws GameActionException {
        roundCaptured = rc.getRoundNum();

        MapLocation cur = rc.getLocation();
        for (int i = 1; i < 8; i+=2) {
            Direction dir = directions[i];
            if (!rc.onTheMap(cur.translate(dir.dx*2, dir.dy*2))) {
                directionsBlocked++;
            }
        }
        boolean gotOne = false;
        for (int i = 0; i < 8; i+=2) {
            Direction dir = directions[i];
            edgeAtDirection[i/2] = !rc.onTheMap(cur.translate(dir.dx*6, dir.dy*6));
            if (edgeAtDirection[i/2]) {
                for (int j = 1; j <= 6; j++) {
                    if (rc.onTheMap(cur.translate(dir.dx*j, dir.dy*j))) {
                        edgeDistance[i/2]++;
                    }
                }
                if (!slandererDirection[Math.floorMod(i - 1,8)]) {
                    slandererDirection[Math.floorMod(i - 1, 8)] = edgeDistance[i / 2] >= 4;
                }
                if (!slandererDirection[Math.floorMod(i + 1, 8)]) {
                    slandererDirection[Math.floorMod(i - 1, 8)] = edgeDistance[i / 2] >= 4;
                }
                if (edgeDistance[i/2] >= 4) {
                    gotOne = true;
                }
            }
        }
        if (!gotOne) {
            slandererDirection = null;
        }

        MapLocation now = rc.getLocation();
        int edgeX = 1, edgeY = 1, dy = 0, dx = 0;
        for (int i = 0; i < 8; i+=2) {
            Direction dir = directions[i];
            if (!rc.onTheMap(cur.translate(dir.dx*6, dir.dy*6))) {
                if (dir.dx != 0) {
                    edgeX = dir.dx;
                }
                if (dir.dy != 0) {
                    edgeY = dir.dy;
                }
            }
        }
        if (now.x%5 == GridHelper.GRID_X) {
            dx = -edgeX;
        }
        if (now.y%5 == GridHelper.GRID_Y) {
            dy = -edgeY;
        }
        switch (dx) {
            case 0:
                switch (dy) {
                    case 0:
                        shiftedTunnel = Direction.CENTER;
                        break;
                    case 1:
                        shiftedTunnel = Direction.NORTH;
                        break;
                    case -1:
                        shiftedTunnel = Direction.SOUTH;
                }
                break;
            case 1:
                switch (dy) {
                    case 0:
                        shiftedTunnel = Direction.EAST;
                        break;
                    case 1:
                        shiftedTunnel = Direction.NORTHEAST;
                        break;
                    case -1:
                        shiftedTunnel = Direction.SOUTHEAST;
                }
                break;
            case -1:
                switch (dy) {
                    case 0:
                        shiftedTunnel = Direction.WEST;
                        break;
                    case 1:
                        shiftedTunnel = Direction.NORTHWEST;
                        break;
                    case -1:
                        shiftedTunnel = Direction.SOUTHWEST;
                }
        }

    }


    private static MapLocation[] relativeLocations;
    private static int cachedRadius = 0, ptr = 0;

    public static MapLocation[] getCircumferencePoints (MapLocation center, int radiusSquared) {
        int rad = (int) Math.sqrt(radiusSquared);
        if (relativeLocations == null || cachedRadius != radiusSquared) {
            relativeLocations = new MapLocation[300];
            ptr = 0;
            for (int x = 1; x <= rad; x++) {
                int limY = (int) Math.sqrt(radiusSquared - x*x);
                relativeLocations[ptr++] = new MapLocation(x, limY);
                relativeLocations[ptr++] = new MapLocation(x, -limY);
                relativeLocations[ptr++] = new MapLocation(-x, limY);
                relativeLocations[ptr++] = new MapLocation(-x, -limY);

                int c = radiusSquared - (x*x + 2*x + 1);
                for (int y = 1-limY; y*y > c && y < 0; y++) {
                    relativeLocations[ptr++] = new MapLocation(x, y);
                    relativeLocations[ptr++] = new MapLocation(x, -y);
                    relativeLocations[ptr++] = new MapLocation(-x, y);
                    relativeLocations[ptr++] = new MapLocation(-x, -y);
                }
            }
            relativeLocations[ptr++] = new MapLocation(rad, 0);
            relativeLocations[ptr++] = new MapLocation(-rad, 0);
            relativeLocations[ptr++] = new MapLocation(0, rad);
            relativeLocations[ptr++] = new MapLocation(0, -rad);
            cachedRadius = radiusSquared;
        }
        MapLocation[] ret = new MapLocation[ptr];
        for (int i = 0; i < ptr; i++) {
            ret[i] = new MapLocation(center.x + relativeLocations[i].x, center.y + relativeLocations[i].y);
        }
        return ret;
    }


    public static void move() throws GameActionException {
         if (roundNumber == 1) {
            StringBuilder sb = new StringBuilder("{ ");
            StringBuilder sbp = new StringBuilder("{ ");
            int sum = 0;
            for (int i = 1; i <= 32; i++) {
                int len = getCircumferencePoints(rc.getLocation(), i * i + 1).length;
                sb.append(len).append(", ");
                sum += len;
                sbp.append(sum).append(", ");
            }
            sb.append("}");
            sbp.append("}");
            log (sb.toString());
            log (sbp.toString());
        }


        if (rc.isReady()) {
            spawnOptimal();
        }

        double factor = Math.min(Math.pow(1.1, rc.getInfluence()/5000.0), 2);
        int bet = (int) (rc.getInfluence() * RATIO_BID * factor);
        if (haveWonInVotes) {
            RATIO_UNITS += RATIO_BID;
        } else if (rc.getRoundNum() >= 150 && rc.canBid(bet)) {
            rc.bid(bet);
        } else if (rc.canBid(1)){
            rc.bid(1);
        }

        EcInfo got = checkForAttackCoordinates();
        if (got != null) {
            if (got.hp >= 0) {
                targetEC = got;
                int dir = directionList.indexOf(rc.getLocation().directionTo(got.location));
                if (dir != -1 && directionsBlocked == 0) {
                    slandererDirection = new boolean[8];
                    Arrays.fill(slandererDirection, true);
                    slandererDirection[Math.floorMod(dir - 1, 8)] =
                            slandererDirection[Math.floorMod(dir + 1, 8)] = slandererDirection[dir] = false;
                }
            } else {
                targetEC = null;
            }
        } else {
            targetEC = null;
        }
    }
}
