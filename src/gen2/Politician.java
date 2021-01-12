package gen2;

import battlecode.common.*;

import static gen2.RobotPlayer.*;
import static gen2.helpers.MovementHelper.*;
import static gen2.helpers.AttackHelper.*;
import static gen2.flags.EnlightenmentCenterFlag.*;


public strictfp class Politician {
    public static MapLocation attackLocation = null;

    public static void init() throws GameActionException {
        for (RobotInfo ri : rc.senseNearbyRobots(2, mTeam)) {
            if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                int flag = rc.getFlag(ri.getID());
                if (isAttackType(flag)) attackLocation = getAttackCoordinates(flag);
                break;
            }
        }
    }

    public static void move() throws GameActionException {
        if (rc.isReady()) {
            if (shouldAttack(attackLocation != null)) {
                rc.empower(actionRadius);
            }
            tryMove(getNextDirection(rc.getLocation(), attackLocation), Precision.MIN);
        }
    }
}
