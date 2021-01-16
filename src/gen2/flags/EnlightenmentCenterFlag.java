package gen2.flags;

import battlecode.common.*;

import static gen2.RobotPlayer.rc;
import static gen2.RobotPlayer.spawnerLocation;


/*
 # EC Flag

 0-6     - enemy/neutral enlightenment center x
 7-13    - enemy/neutral enlightenment center y
 14      - attack/ defense politician
 15-23   - undecided

 */

public class EnlightenmentCenterFlag {

    public static void broadcastAttackCoordinates (MapLocation loc) throws GameActionException {
        int relX = loc.x - spawnerLocation.x + 63,
                relY = loc.y - spawnerLocation.y + 63;

        int newFlag = 1 << 14;
        newFlag += relX;
        newFlag += relY << 7;
        rc.setFlag(newFlag);
    }

    public static boolean isAttackType (int flag) {
        return (flag & (1<<14)) == (1<<14);
    }

    public static void setDefensePoliticianSpawned() throws GameActionException {
        int flag = rc.getFlag(rc.getID());
        rc.setFlag(flag ^ (1 << 14));
    }

    public static MapLocation getAttackCoordinates (int flag) {
        int relX = (flag) % 128 - 63,
                relY = (flag >> 7) % 128 - 63;
        return new MapLocation(relX + spawnerLocation.x, relY + spawnerLocation.y);
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {

    }
}
