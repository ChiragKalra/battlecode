package gen5;

import battlecode.common.*;
import gen5.helpers.GridHelper;
import gen5.util.Pair;
import gen5.util.SpawnType;

import static gen5.RobotPlayer.*;
import static gen5.helpers.AttackHelper.checkForAttackCoordinates;
import static gen5.helpers.MovementHelper.directions;
import static gen5.helpers.SpawnHelper.*;
import static gen5.util.Functions.sigmoid;
import static gen5.util.SpawnType.getOptimalType;


public strictfp class EnlightenmentCenter {

    public static int currentRadius = 3;

    public static double RATIO_BID = 0.08;
    public static double RATIO_UNITS = 0.02;

    public static int roundCaptured = 1;
    public static int directionsBlocked = 0;
    public static Direction shiftedTunnel = Direction.CENTER;
    public static Pair<MapLocation, Integer> targetEC;


    private static boolean spawnOptimal() throws GameActionException {
        boolean spawned = false;
        int roundsMine = roundNumber - roundCaptured;
        if (roundNumber % 17 == 1 && roundsMine > 100*sigmoid((roundCaptured-750)/200.0)) {
            spawned = spawnSlanderer();
        }

        if (!spawned) {
            SpawnType got = getOptimalType();
            if (got != null) {
                switch (got) {
                    case Muckraker:
                        spawned = spawnMuckraker();
                        break;
                    case DefensePolitician:
                        spawned = spawnDefencePolitician();
                        break;/*
                    case Slanderer:
                        spawned = spawnSlanderer();
                        break;*/
                }
                if (!spawned && rc.senseNearbyRobots(sensorRadius, enemyTeam).length>0) {
                    spawned = spawnDefencePolitician();
                }
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
        if (now.x%5 == GridHelper.MUCKRAKER_GRID_X) {
            dx = -edgeX;
        }
        if (now.y%5 == GridHelper.MUCKRAKER_GRID_X) {
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

    public static void move() throws GameActionException {
        if (rc.isReady()) {
            spawnOptimal();
        }

        double factor = Math.min(Math.pow(1.1, rc.getInfluence()/5000.0), 2);
        int bet = (int) (rc.getInfluence() * RATIO_BID * factor);
        if (rc.getRoundNum() >= 250 && rc.canBid(bet) && rc.getTeamVotes() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            rc.bid(bet);
        } else if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS/2) {
            RATIO_UNITS += RATIO_BID;
        } else if (rc.getRoundNum() < 250 && rc.canBid(1)){
            rc.bid(1);
        }

        Pair<MapLocation, Integer> got = checkForAttackCoordinates();
        if (got != null) {
            if (got.value >= 0) {
                targetEC = got;
            }
        }
    }
}
