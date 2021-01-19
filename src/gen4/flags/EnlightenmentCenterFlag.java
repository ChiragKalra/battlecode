package gen4.flags;

import battlecode.common.GameActionException;

import static gen4.RobotPlayer.rc;
import static gen4.EnlightenmentCenter.currentRadius;
import static gen4.RobotPlayer.roundNumber;

/*
 # EC flag
 0-4	- wall radius
 5-23	- vacant
 */

public class EnlightenmentCenterFlag {

	public static int getRadius(int flag) {
		return flag & 63;
	}

    // layerQuantity = { 0, 8, 12, 16, 20, 28, 32, 40, 44, 48, 56, 60, 68, 72, 76, 84, };
    // private static final int[] sectorQuantity = { 0, 8, 20, 36, 56, 84, 116, 156, 200, 248, 304, 364, 432, 504, 580, 664, };
	private static final int[] roundExpanded = new int[64];

    // check for flag changes and set flag
    private static int blockedRounds = 0;
    public static void updateFlag() throws GameActionException {
        if (rc.senseNearbyRobots(10).length >= 14) {
            blockedRounds++;
        } else {
            blockedRounds = 0;
        }

        if (blockedRounds >= 10 && currentRadius<63 &&
                roundNumber-roundExpanded[currentRadius-1] >= 6*currentRadius) {
            roundExpanded[currentRadius++] = roundNumber;
            blockedRounds = 0;
        }

        int prevFlag = rc.getFlag(rc.getID());
        int newFlag = ((prevFlag >> 6) << 6) | currentRadius;
        if (prevFlag != newFlag) {
            rc.setFlag(newFlag);
        }
    }
}
