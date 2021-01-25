package gen5.flags;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import gen5.helpers.MovementHelper;

import static gen5.EnlightenmentCenter.*;
import static gen5.RobotPlayer.rc;
import static gen5.helpers.MovementHelper.directionList;
import static gen5.helpers.MovementHelper.directions;
import static gen5.helpers.SpawnHelper.shouldDecrementWallRadius;
import static gen5.helpers.SpawnHelper.shouldIncrementWallRadius;
import static gen5.util.Functions.getBits;
import static gen5.util.Functions.setBits;

/*
 # EC flag
 0-5	- wall radius
 6-9    - tunnel reference shift direction
 10-20  - Vacant
 20-23	- Buff Muck Direction
 */

public class EnlightenmentCenterFlag {

	public static int getRadius(int flag) {
		return flag & 63;
	}

	public static Direction getShiftDirection(int flag) {
	    int dirInt = (flag >> 6) & 15;
	    if (dirInt == 8) return Direction.CENTER;
		return directions[dirInt];
	}

    public static Direction getBuffMuckrakerDirection (int flag) {
        int dirInt = getBits(flag, 23, 20);
        if (dirInt == 0) return null;
        return MovementHelper.directions[dirInt-1];
    }

    /*
    * TODO
    *  @return
    *       null if no weak direction
    *
    *
    * */
    public static Direction getWeakWallDirection (int flag) {
	    return null;
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
	    if (shouldDecrementWallRadius()) {
            currentRadius--;
        } else if (shouldIncrementWallRadius()) {
            currentRadius++;
        }

        int prevFlag = rc.getFlag(rc.getID());
        int newFlag = 0;
        newFlag += currentRadius;

        int shiftInt = 8;
        if (shiftedTunnel != Direction.CENTER) {
            shiftInt = directionList.indexOf(shiftedTunnel);
        }
        newFlag += shiftInt << 6;


        int dirInt = 0;
        if (buffMuckApproachDirection != null) {
            dirInt = MovementHelper.directionList.indexOf(buffMuckApproachDirection) + 1;
        }
        newFlag = setBits(newFlag, 23, 20, dirInt);

        if (prevFlag != newFlag) {
            rc.setFlag(newFlag);
        }
    }
}
