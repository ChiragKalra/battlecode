package gen3.flags;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static gen2.RobotPlayer.spawnerLocation;


/*
 # Muckraker Flag

 0-6    - enemy/neutral enlightenment center x
 7-13   - enemy/neutral enlightenment center y
 14-21  - enlightenment center hp:- log10(hp)
 22     - broadcasting coordinates
 23

 */

public class PoliticianFlag {
    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {

    }

    public static boolean isBroadcastingEnemyEC(int flag) {
        if ((flag & 8) == 8) {
            return false;
        }
        return flag % 8 == 3;
    }

    public static MapLocation getCoordinatesFromFlag(int flag) {
        int relX = (flag >> 7) % 128 - 63,
                relY = (flag >> 14) % 128 - 63;
        return new MapLocation(relX + spawnerLocation.x, relY + spawnerLocation.y);
    }

    public static int getNeutralHpFromFlag(int flag) {
        return ((flag >> 21) % 8)*50 + 150;
    }
}
