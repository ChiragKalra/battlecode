package gen3.flags;

import battlecode.common.GameActionException;

import static gen3.RobotPlayer.rc;
import static gen3.EnlightenmentCenter.currentRadius;

/*
 # EC flag
 0-4	- wall radius
 5-23	- vacant
 */

public class EnlightenmentCenterFlag {

	public static void incrementRadius() throws GameActionException {
		++currentRadius;
		int flag = rc.getFlag(rc.getID());
		flag = ((flag >> 5) << 5) | currentRadius;
		rc.setFlag(flag);
	}

	public static int getRadius(int flag) {
		return flag & 31;
	}

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
    	int prevFlag = rc.getFlag(rc.getID());
    	int newFlag = ((prevFlag >> 5) << 5) | currentRadius;
    	if (prevFlag != newFlag) {
    		rc.setFlag(newFlag);
    	}
    }
}
