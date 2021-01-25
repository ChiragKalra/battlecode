package gen5.helpers;

import battlecode.common.*;

import gen5.flags.DefensePoliticianFlag;
import gen5.util.*;

import java.util.Iterator;

import static gen5.EnlightenmentCenter.*;
import static gen5.RobotPlayer.*;
import static gen5.helpers.GridHelper.getDirectionFromAdjacentFlags;
import static gen5.helpers.MovementHelper.*;
import static gen5.util.Functions.getBits;

public class SpawnHelper {

    private static final int LIMIT_WALL_RADIUS = 32;

    public static int slandererHPFloor (int hp) {
        double func = (0.02 + 0.03*Math.exp(-0.001*hp))*hp;
        return (int) Math.ceil((Math.floor(func)*hp)/func);
    }

    public static Vector<Direction> getWeakDirections() {
        Vector<Direction> dirs = new Vector<>(null, 4);
        if (defensePoliticiansInDirection == null) {
            return dirs;
        }
        for (int i = 0; i < 4; i++) {
            int count = defensePoliticiansInDirection[i];
            if (count < 1.25*currentRadius) {
                dirs.add(directions[i*2+1]);
            }
        }
        return dirs;
    }


    private static final int[] layerQuantity = {
            0, 8, 12, 16, 20, 28, 32, 40, 44, 48, 56, 60, 68, 72, 76, 84, 88, 96,
            100, 104, 112, 116, 124, 128, 132, 140, 144, 152, 156, 164, 168, 172, 180,
    };
/*
    private static final int[] sectorQuantity = {
            0, 8, 20, 36, 56, 84, 116, 156, 200, 248, 304, 364, 432, 504, 580, 664, 752, 848, 948,
            1052, 1164, 1280, 1404, 1532, 1664, 1804, 1948, 2100, 2256, 2420, 2588, 2760, 2940,
    };
*/

    public static int[] defensePoliticiansInDirection = null;
    private static int blockedRounds = 0;
    public static boolean shouldIncrementWallRadius() {
        int capacity  = 20;
        switch (directionsBlocked) {
            case 2:
                capacity = 12;
                break;
            case 3:
                capacity = 7;
        }
        if (rc.senseNearbyRobots(10, mTeam).length >= capacity-2) {
            blockedRounds++;
        } else {
            blockedRounds = 0;
        }
        boolean ans = blockedRounds >= 10 && currentRadius<LIMIT_WALL_RADIUS && (
                currentRadius < 7 || defencePoliticians.getSize() > 10*currentRadius*(directionsBlocked/4.0));
        if (ans) {
            blockedRounds = 0;
        }
        return ans;
    }

    private static int roundCached = 0;
    private static boolean shouldDecrementRadius = false;
    public static boolean shouldDecrementWallRadius() throws GameActionException {
        // return defencePoliticians.getSize() < 7*currentRadius*(directionsBlocked/4.0);
        if (roundCached == roundNumber) {
            return shouldDecrementRadius;
        }
        roundCached = roundNumber;

        if (currentRadius >= LIMIT_WALL_RADIUS) {
            return shouldDecrementRadius = true;
        }
        if (currentRadius <= 6) {
            return shouldDecrementRadius = false;
        }

        Direction buffMuckraker = null;
        // 0 - NE, 1 - SE, 2 - SW, 3 - NW
        int[] politiciansCount = new int[4];
        // MapLocation origin = new MapLocation(spawnerLocation.x + shiftedTunnel.dx, spawnerLocation.y + shiftedTunnel.dy);
        Iterator<Integer> iter = defencePoliticians.iterator();
        while (iter.hasNext()) {
            int id = iter.next();
            if (!rc.canGetFlag(id)) {
                iter.remove();
                continue;
            }

            int flag = rc.getFlag(id);
            // not on wall or on tunnel point
            if (getBits(flag, 0, 0) == 0 || getBits(flag, 1, 1) == 1) {
                continue;
            }
            if (buffMuckraker == null) {
                Direction got = DefensePoliticianFlag.getBuffMuckrakerDirection(flag);
                if (got != null) {
                    buffMuckraker = got;
                }
            }
            ++politiciansCount[getBits(flag, 3, 2)];
        }

        buffMuckApproachDirection = buffMuckraker;

       /* System.out.println(currentRadius);
        for (int i = 0; i < 4; ++i)
            System.out.println(politiciansCount[i]);
*/
        shouldDecrementRadius = false;
        int each = (layerQuantity[currentRadius] - 4) / 4 + (layerQuantity[currentRadius + 1] - 4) / 4;
        // North-east
        if (edgeAtDirection[0] && !edgeAtDirection[1]) {
            int edgeDist = edgeDistance[0] - shiftedTunnel.dy;
            int required = edgeDist * 2;
            if (politiciansCount[0] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[0] && edgeAtDirection[1]) {
            int edgeDist = edgeDistance[1] - shiftedTunnel.dx;
            int required = edgeDist * 2;
            if (politiciansCount[0] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[0]) {
            if (politiciansCount[0] < each) {
                return shouldDecrementRadius = true;
            }
        }

        // South-east
        if (edgeAtDirection[2] && !edgeAtDirection[1]) {
            int edgeDist = edgeDistance[2] + shiftedTunnel.dy;
            int required = edgeDist * 2;
            if (politiciansCount[1] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[2] && edgeAtDirection[1]) {
            int edgeDist = edgeDistance[1] - shiftedTunnel.dx;
            int required = edgeDist * 2;
            if (politiciansCount[1] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[2]) {
            if (politiciansCount[1] < each) {
                return shouldDecrementRadius = true;
            }
        }

        // South-west
        if (edgeAtDirection[2] && !edgeAtDirection[3]) {
            int edgeDist = edgeDistance[2] + shiftedTunnel.dy;
            int required = edgeDist * 2;
            if (politiciansCount[2] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[2] && edgeAtDirection[3]) {
            int edgeDist = edgeDistance[3] + shiftedTunnel.dx;
            int required = edgeDist * 2;
            if (politiciansCount[2] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[2]) {
            if (politiciansCount[2] < each) {
                return shouldDecrementRadius = true;
            }
        }

        // North-west
        if (edgeAtDirection[0] && !edgeAtDirection[3]) {
            int edgeDist = edgeDistance[0] - shiftedTunnel.dy;
            int required = edgeDist * 2;
            if (politiciansCount[3] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[0] && edgeAtDirection[3]) {
            int edgeDist = edgeDistance[3] + shiftedTunnel.dx;
            int required = edgeDist * 2;
            if (politiciansCount[3] < required) {
                return shouldDecrementRadius = true;
            }
        } else if (!edgeAtDirection[0]) {
            if (politiciansCount[3] < each) {
                return shouldDecrementRadius = true;
            }
        }
        defensePoliticiansInDirection = politiciansCount;
        return false;
    }

    private static int spawnDirectionGridPol = 0;
    public static boolean spawnGridPolitician() throws GameActionException {
        spawnDirectionGridPol = (spawnDirectionGridPol+2)%8;
        while (edgeAtDirection[spawnDirectionGridPol/2]) {
            spawnDirectionGridPol = (spawnDirectionGridPol+2)%8;
        }
        Direction got = getDirectionFromAdjacentFlags(rc.getLocation()),
                dir = getOptimalDirection(got != null ? got : directions[spawnDirectionGridPol]);
        if (dir == null ) {
            return false;
        }

        int xp = (int)(rc.getInfluence()*RATIO_UNITS/2);
        xp = Math.max(SpawnType.GridPolitician.minHp, xp);
        xp = Math.min(SpawnType.GridPolitician.maxHp, xp);

        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            return true;
        }
        return false;
    }


    public static boolean spawnMuckraker(MapLocation toAttack, boolean buff) throws GameActionException {
        Direction got = toAttack == null ? null : rc.getLocation().directionTo(toAttack),
                rand = directions[(int) (Math.random() * 4) * 2],
                dir = getOptimalDirection(got != null ? got : rand);
        if (dir == null ) {
            return false;
        }

        int xp = 1;
        if (buff) {
            xp = Math.max(SpawnType.BuffMuckraker.minHp, xp);
            xp = Math.min(SpawnType.BuffMuckraker.maxHp, xp);
        } else {
            xp = Math.max(SpawnType.Muckraker.minHp, xp);
            xp = Math.min(SpawnType.Muckraker.maxHp, xp);
        }

        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            return true;
        }
        return false;
    }


    public static boolean spawnFillerMuckraker() throws GameActionException {
        Direction rand = directions[(int) (Math.random() * 4) * 2],
                dir = getOptimalDirection(rand);
        if (dir == null ) {
            return false;
        }

        int xp = 1;
        xp = Math.max(SpawnType.FillerMuckraker.minHp, xp);
        xp = Math.min(SpawnType.FillerMuckraker.maxHp, xp);


        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, xp)) {
            rc.buildRobot(RobotType.MUCKRAKER, dir, xp);
            return true;
        }
        return false;
    }

    public static boolean spawnAttackPolitician (MapLocation toAttack, int hp) throws GameActionException {
        Direction dir = getOptimalDirection(rc.getLocation().directionTo(toAttack));
        if (dir == null) {
            return false;
        }
        int xp = hp + 11,
            minXp = SpawnType.AttackPolitician.minHp,
            maxXp = SpawnType.AttackPolitician.maxHp;
        xp = Math.max(minXp, xp);
        xp = Math.min(maxXp, xp);
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            return true;
        }
        return false;
    }

    private static Pair<Direction, Boolean> getEnemyEcOrMuck () {
        boolean muck = false;
        Direction direction = null;
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
            if (ri.type == RobotType.MUCKRAKER) {
                muck = true;
            } else if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                direction = ri.location.directionTo(spawnerLocation);
            }
        }
        return new Pair<>(direction, muck);
    }

    private static final int slanMinXp = slandererHPFloor(SpawnType.Slanderer.minHp),
            slanMaxXp = slandererHPFloor(SpawnType.Slanderer.maxHp);
    public static boolean spawnSlanderer() throws GameActionException {
        Direction dir = null;
        if (slandererDirection != null) {
            for (int d = 1; d < 8; d += 2) {
                if (slandererDirection[d]) {
                    dir = directions[d];
                    break;
                }
            }
        }
        if (dir == null) {
            dir = directions[(int) (Math.random() * 4) * 2 + 1];
        }

        Pair<Direction, Boolean> got = getEnemyEcOrMuck();
        if (got.value) {
            return false;
        }
        if (got.key != null) {
            dir = got.key;
        }

        dir = getOptimalDirection(dir);
        if (dir == null) {
            return false;
        }
        int xp = slandererHPFloor(rc.getInfluence());
        xp = Math.max(slanMinXp, xp);
        xp = Math.min(slanMaxXp, xp);
        if (rc.canBuildRobot(RobotType.SLANDERER, dir, xp)) {
            rc.buildRobot(RobotType.SLANDERER, dir, xp);
            return true;
        }
        return false;
    }


    private static int spawnDirectionDefPol = 0;
    public static LinkedList<Integer> defencePoliticians = new LinkedList<>();
    public static boolean spawnDefencePolitician() throws GameActionException {
        Vector<Direction> got = getWeakDirections();

        spawnDirectionDefPol = (spawnDirectionDefPol+2)%8;
        while (edgeAtDirection[spawnDirectionDefPol/2]) {
            spawnDirectionDefPol = (spawnDirectionDefPol+2)%8;
        }

        Direction dir = getOptimalDirection (
                buffMuckApproachDirection != null ?
                        buffMuckApproachDirection : directions[spawnDirectionDefPol]
        );
        if (got.length != 0) {
            dir = getOptimalDirection(got.get((int)(Math.random()*got.length)));
        }
        if (dir == null ) {
            return false;
        }
        int xp = buffMuckApproachDirection != null ?
                SpawnType.DefensePolitician.maxHp : (int)(rc.getInfluence()*0.008);
        xp = Math.max(SpawnType.DefensePolitician.minHp, xp);
        xp = Math.min(SpawnType.DefensePolitician.maxHp, xp);
        if (rc.canBuildRobot(RobotType.POLITICIAN, dir, xp)) {
            rc.buildRobot(RobotType.POLITICIAN, dir, xp);
            defencePoliticians.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
            return true;
        }
        return false;
    }


    public static Direction getOptimalDirection (Direction to) throws GameActionException {
        MapLocation current = rc.getLocation();
        if (to == null) {
            to = Direction.SOUTHEAST;
        }
        int dirInt = directionList.indexOf(to);
        // if blocked by another robot, find the next best direction
        for (int i = 0; i < 5; i++) {
            Direction got = directions[Math.floorMod(dirInt + i, 8)];
            MapLocation ml = current.add(got);
            if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                return got;
            }
            got = directions[Math.floorMod(dirInt - i, 8)];
            ml = current.add(got);
            if (rc.onTheMap(ml) && !rc.isLocationOccupied(ml)) {
                return got;
            }
        }
        return null;
    }

}
