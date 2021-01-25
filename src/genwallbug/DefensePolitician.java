package gen5;

import battlecode.common.*;
import gen5.flags.EnlightenmentCenterFlag;
import gen5.util.Pair;
import gen5.util.SpawnType;

import static gen5.RobotPlayer.*;
import static gen5.flags.EnlightenmentCenterFlag.*;
import static gen5.helpers.AttackHelper.*;
import static gen5.helpers.DefenseHelper.*;
import static gen5.helpers.MovementHelper.*;
import static gen5.util.Functions.getBits;

public strictfp class DefensePolitician {

    public static int hasChangedEc = 0;
    public static Direction buffMuckrakerDirection = null;

    private static void approachOrEmpowerMuck (int radius) throws GameActionException {
        Direction d = getApproachDirection();
        if (d == null) {
            rc.empower(radius);
        } else {
            tryMove(d, true);
        }
    }

    public static int radius = 3;

    public static void move() throws GameActionException {
        for (RobotInfo ri: rc.senseNearbyRobots(sensorRadius, mTeam)) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER && spawnerLocation != ri.location) {
                enlightenmentCenterId = ri.getID();
                spawnerLocation = ri.location;
                hasChangedEc = 2;
            }
        }

        // is a muckraker
        if (spawnType != SpawnType.DefensePolitician) {
            MapLocation locToExpose = null;
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemyTeam)) {
                if (robot.type.canBeExposed()) {
                    if (robot.location.isWithinDistanceSquared(rc.getLocation(), actionRadius)) {
                        // expose the slanderer
                        if (rc.canExpose(robot.location)) {
                            rc.expose(robot.location);
                            return;
                        }
                    } else {
                        locToExpose = robot.location;
                    }
                }
            }

            if (locToExpose != null) {
                goTo(locToExpose);
                return;
            }
        }

        int flag;
        if (rc.canGetFlag(enlightenmentCenterId)) {
            flag = rc.getFlag(enlightenmentCenterId);
            radius = getRadius(flag);
        } else {
            if (spawnType == SpawnType.DefensePolitician) {
                spawnType = SpawnType.AttackPolitician;
                AttackPolitician.move();
            }
            return;
        }

        int innerRadius = radius * radius + 1;
        int outerRadius = (radius + 1) * (radius + 1) + 1;

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

        MapLocation center = new MapLocation(spawnerLocation.x + tunnelShift.dx, spawnerLocation.y + tunnelShift.dy);
        boolean onInnerWall = isWallOfRadius(center, rc.getLocation(), innerRadius);
        boolean onOuterWall = isWallOfRadius(center, rc.getLocation(), outerRadius);
        boolean isOnWall = onInnerWall || onOuterWall;
        boolean isOutsideWall = outsideWall(rc.getLocation(), outerRadius);

        Pair<Integer, Boolean> p = shouldAttackDefensive();
        Integer rad = p.key;
        Boolean muckrakerAdjacent = p.value;

        if (rad != 0 && spawnType == SpawnType.DefensePolitician) {
            // only >= third layer will explode normally (except when a muckraker is adjacent)
            if (isOutsideWall || muckrakerAdjacent) {
                approachOrEmpowerMuck(rad);
                return;
            }

            // second layer will explode if there's no third layer
            // first layer will explode if there's no second layer
            if (isOnWall) {
                int defensePoliticianCount = (isDefensePolitician(straight) ? 1 : 0) + (isDefensePolitician(left) ? 1 : 0) +
                                                (isDefensePolitician(right) ? 1 : 0);
                if (defensePoliticianCount < 2) {
                    approachOrEmpowerMuck(rad);
                }
                return;
            }

            // politicians inside the wall will explode
            approachOrEmpowerMuck(rad);
            return;
        }


        if (buffMuckrakerDirection != null) {
            goTo(
                spawnerLocation.translate(
                    buffMuckrakerDirection.dx*(radius+1),
                    buffMuckrakerDirection.dy*(radius+1)
                )
            );
            return;
        }

        if (getBits(flag, 10, 10) == 1) {
            MapLocation now = rc.getLocation();
            int weakQuadrant = getBits(flag, 12, 11);
            int strongQuadrant = getBits(flag, 14, 13);
            int quadrant;
            if (spawnerLocation.x < now.x) {
                if (spawnerLocation.y < now.y) {
                    quadrant = 0;
                } else {
                    quadrant = 1;
                }
            } else {
                if (spawnerLocation.y < now.y) {
                    quadrant = 3;
                } else {
                    quadrant = 2;
                }
            }
             // = (new MapLocation(0,0)).directionTo(
             //                new MapLocation(
             //                        spawnerLocation.x < now.x ? 1 : -1,
             //                        spawnerLocation.y < now.y ? 1 : -1
             //                        )
             //        );
            // if (got != null && !got.equals(quadrant)) {
            //     goTo(spawnerLocation.translate(got.dx * radius, got.dy * radius));
            //     return;
            // }

            Direction got = Direction.NORTHEAST;
            if (quadrant == 1) {
                got = Direction.SOUTHEAST;
            } else if (quadrant == 2) {
                got = Direction.SOUTHWEST;
            } else if (quadrant == 3) {
                got = Direction.NORTHWEST;
            }

            if (quadrant != weakQuadrant) {
                goTo(spawnerLocation.translate(got.dx * 4, got.dy * 4));
                return;
            }
        }

        if (isOnWall) {
            if (!isTunnelPoint(rc.getLocation())) {
                return;
            }
            
            if (forceMove(left.rotateLeft())) {
                return;
            }
            if (forceMove(right.rotateRight())) {
                return;
            }
            if (forceMove(left)) {
                return;
            }
            if (forceMove(right)) {
                return;
            }
            if (forceMove(straight)) {
                return;
            }

            return;
        }

        if (isOutsideWall) {
            Direction opposite = straight.opposite();
            Direction oppLeft = opposite.rotateLeft();
            Direction oppRight = opposite.rotateRight();

            if (forceMoveWall(oppRight)) {
                return;
            }
            if (forceMoveWall(oppLeft)) {
                return;
            }
            if (forceMoveWall(oppRight.rotateRight())) {
                return;
            }
            if (forceMoveWall(oppLeft.rotateLeft())) {
                return;
            }

            if (isTunnelPoint(rc.getLocation())) {
                if (forceMove(left)) {
                    return;
                }
                if (forceMove(right)) {
                    return;
                }
                if (forceMove(straight)) {
                    return;
                }
            } else if (forceMoveWall(opposite)) {
                return;
            }

            return;
        }

        if (tryMoveWall(straight, outerRadius)) {
            return;
        }
        if (tryMoveWall(left, outerRadius)) {
            return;
        }
        if (tryMoveWall(right, outerRadius)) {
            return;
        }

        for (int i = 0; i < 2; ++i) {
            left = left.rotateLeft();
            if (tryMoveWall(left, outerRadius)) {
                return;
            }

            right = right.rotateRight();
            if (tryMoveWall(right, outerRadius)) {
                return;
            }
        }

        left = left.rotateLeft();
        tryMoveWall(left, outerRadius);

    }

    private static boolean isDefensePolitician(Direction dir) throws GameActionException {
        MapLocation loc = rc.getLocation().add(dir);
        if (!rc.onTheMap(loc)) {
            return false;
        }
        RobotInfo robot = rc.senseRobotAtLocation(loc);
        if (robot == null) {
            return false;
        }
        return robot.team == mTeam && robot.type == RobotType.POLITICIAN && 
                robot.getConviction() >= SpawnType.DefensePolitician.minHp &&
                robot.getConviction() <= SpawnType.DefensePolitician.maxHp;
    }

    public static void init() throws GameActionException {
        if (rc.canGetFlag(enlightenmentCenterId)) {
            int flag = rc.getFlag(enlightenmentCenterId);
            tunnelShift = getShiftDirection(flag);
            buffMuckrakerDirection = EnlightenmentCenterFlag.getBuffMuckrakerDirection(flag);
        }
    }
}
