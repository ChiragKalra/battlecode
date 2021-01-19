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
		return flag & 63;
	}

    // check for flag changes and set flag
    private static int blockedRounds = 0;
    public static void updateFlag() throws GameActionException {
        if (rc.senseNearbyRobots(10).length >= 15) {
            blockedRounds++;
        } else {
            blockedRounds = 0;
        }

        if (blockedRounds >= 10 && currentRadius<64) {
            currentRadius++;
            blockedRounds = 0;
        }

        int prevFlag = rc.getFlag(rc.getID());
        int newFlag = ((prevFlag >> 6) << 6) | currentRadius;
        if (prevFlag != newFlag) {
            rc.setFlag(newFlag);
        }
    }
}
