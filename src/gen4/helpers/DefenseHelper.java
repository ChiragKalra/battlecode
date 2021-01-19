package gen4.helpers;

import battlecode.common.*;

import static gen4.RobotPlayer.enlightenmentCenterId;
import static gen4.RobotPlayer.rc;
import static gen4.RobotPlayer.spawnerLocation;
import static gen4.flags.EnlightenmentCenterFlag.getRadius;
import static gen4.helpers.AttackHelper.shouldAttackDefensive;
import static gen4.helpers.MovementHelper.getCircumferencePoints;
import static gen4.Politician.*;

public class DefenseHelper {


	public static int innerRadius, outerRadius;
	public static MapLocation[] innerPoints, outerPoints;

	public static void moveDefense () throws GameActionException {
		int rad = shouldAttackDefensive();
		if (rad != 0) {
			rc.empower(rad);
			return;
		}

		int radius;
		if (rc.canGetFlag(enlightenmentCenterId)) {
			radius = getRadius(rc.getFlag(enlightenmentCenterId));
		} else {
			isAttackType = true;
			return;
		}
		innerRadius = radius * radius + 1;
		outerRadius = (radius + 1) * (radius + 1) + 1;

		innerPoints = getCircumferencePoints(spawnerLocation, innerRadius);
		outerPoints = getCircumferencePoints(spawnerLocation, outerRadius);

		Direction straight = rc.getLocation().directionTo(spawnerLocation).opposite();
		Direction left = straight.rotateLeft();
		Direction right = straight.rotateRight();

		if (onWall(rc.getLocation()) && !isTunnelPoint(rc.getLocation())) {
			return;
		}
		if (outsideWall(rc.getLocation())) {
			Direction opposite = straight.opposite();
			Direction oppleft = opposite.rotateLeft();
			Direction oppright = opposite.rotateRight();

			if (forceMove(oppright)) {
				return;
			}
			if (forceMove(oppleft)) {
				return;
			}
			if (forceMove(oppright.rotateRight())) {
				return;
			}
			if (forceMove(oppleft.rotateLeft())) {
				return;
			}

			return;
		}

		if (tryMoveWall(straight)) {
			return;
		}
		if (tryMoveWall(left)) {
			return;
		}
		if (tryMoveWall(right)) {
			return;
		}

		if (onWall(rc.getLocation())) {
			if (forceMove(straight)) {
				return;
			}
			if (forceMove(left)) {
				return;
			}
			if (forceMove(right)) {
				return;
			}
			if (forceMove(left.rotateLeft())) {
				return;
			}
			if (forceMove(right.rotateRight())) {
				return;
			}

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

	public static boolean onWall(MapLocation ml) throws GameActionException {
		return contains(innerPoints, ml) || contains(outerPoints, ml);
	}

	public static boolean isTunnelPoint(MapLocation ml) {
		return ml.x == spawnerLocation.x || ml.y == spawnerLocation.y || 
				Math.abs(ml.x - spawnerLocation.x) == Math.abs(ml.y - spawnerLocation.y);
	}

	public static boolean contains(MapLocation[] mapLocations, MapLocation ml) throws GameActionException {
		for (MapLocation mapLocation : mapLocations) {
			if (mapLocation.equals(ml)) {
				return true;
			}
		}

		return false;
	}

	public static boolean tryMoveWall(Direction dir) throws GameActionException {
		MapLocation ml = rc.getLocation().add(dir);
		if (outsideWall(ml)) {
			return false;
		}

    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }

    public static boolean outsideWall(MapLocation ml) {
    	return ml.distanceSquaredTo(spawnerLocation) > outerRadius;
    }

    public static boolean forceMove(Direction dir) throws GameActionException {
    	if (rc.canMove(dir)) {
    		rc.move(dir);
    		return true;
    	}
    	return false;
    }
}
