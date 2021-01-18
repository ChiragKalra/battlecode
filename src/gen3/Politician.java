package gen3;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import gen3.util.Pair;
import gen3.util.SpawnType;

import static gen3.RobotPlayer.rc;
import static gen3.helpers.AttackHelper.*;
import static gen3.helpers.MovementHelper.*;
import static gen3.flags.EnlightenmentCenterFlag.getRadius;
import static gen3.RobotPlayer.*;
import static gen3.helpers.DefenseHelper.*;

public strictfp class Politician {
    public static boolean isAttackType = false;

    public static void init() {
        isAttackType = rc.getInfluence() % 2 == 1;
    }

    private static MapLocation locToEmp = null;
    private static void moveOffense () throws GameActionException {
        if (shouldAttackOffensive()) {
            rc.empower(1);
        } else {
            if (locToEmp == null) {
                locToEmp = getOptimalLocationToEmpower();
            }
            if (locToEmp != null) {
                tryMove(rc.getLocation().directionTo(locToEmp));
            } else {
                Pair<MapLocation, Integer> got = checkForAttackCoordinates();
                if (got != null && got.value >= 0) {
                    MapLocation attackLocation = got.key;
                    if (attackLocation == null || tryMove(rc.getLocation().directionTo(attackLocation))) {
                        tryMove(getNextDirection(attackLocation));
                    }
                } else {
                    tryMove(getNextDirection(null));
                }
            }
        }
    }

    public static int innerRadius, outerRadius;
    public static MapLocation[] innerPoints, outerPoints;

    private static void moveDefense () throws GameActionException {
        int rad = shouldAttackDefensive();
        if (rad != 0) {
            rc.empower(rad);
            return;
        }

        int radius = getRadius(rc.getFlag(enlightenmentCenterId));
        innerRadius = radius * radius + 1;
        outerRadius = (radius + 1) * (radius + 1) + 1;

        innerPoints = getCircumferencePoints(spawnerLocation, innerRadius);
        outerPoints = getCircumferencePoints(spawnerLocation, outerRadius);

        if (onWall(rc.getLocation()) && !isTunnelPoint(rc.getLocation())) {
            return;
        }

        Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
        Direction left = straight.rotateLeft();
        Direction right = straight.rotateRight();

        if (tryMoveWall(straight)) {
            return;
        }
        if (tryMoveWall(left)) {
            return;
        }
        if (tryMoveWall(right)) {
            return;
        }

        for (int i = 0; i < 2; ++i) {
            left = left.rotateLeft();
            if (tryMoveWall(left)) {
                return;
            }

            right = right.rotateRight();
            if (tryMoveWall(right)) {
                return;
            }
        }

        left = left.rotateLeft();
        tryMoveWall(left);
    }

    public static void move() throws GameActionException {
        if (rc.isReady()) {
            if (isAttackType) {
               moveOffense();
            } else {
                moveDefense();
            }
        }
    }
}
