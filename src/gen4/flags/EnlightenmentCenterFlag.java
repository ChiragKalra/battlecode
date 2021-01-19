package gen4.flags;

import battlecode.common.GameActionException;

import static gen4.RobotPlayer.rc;
import static gen4.EnlightenmentCenter.currentRadius;

/*
 # EC flag
 0-4	- wall radius
 5-23	- vacant
 */

public class EnlightenmentCenterFlag {

	public static int getRadius(int flag) {
		return flag & 31;
	}

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
		if (rc.senseNearbyRobots(5).length > 16) {
			currentRadius++;
		}

    	int prevFlag = rc.getFlag(rc.getID());
    	int newFlag = ((prevFlag >> 5) << 5) | currentRadius;
    	if (prevFlag != newFlag) {
    		rc.setFlag(newFlag);
    	}
    }
}
