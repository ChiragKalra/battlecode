package gen5.flags;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import static gen5.EnlightenmentCenter.shiftedTunnel;
import static gen5.RobotPlayer.rc;
import static gen5.EnlightenmentCenter.currentRadius;
import static gen5.helpers.SpawnHelper.*;
import static gen5.helpers.MovementHelper.*;

/*
 # EC flag
 0-5	- wall radius
 6-9    - tunnel reference shift direction
 9-23	- vacant
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

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
        if (shouldIncrementWallRadius()) {
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

        if (prevFlag != newFlag) {
            rc.setFlag(newFlag);
        }
    }
}
