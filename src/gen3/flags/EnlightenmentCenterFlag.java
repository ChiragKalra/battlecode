package gen3.flags;

import battlecode.common.GameActionException;

import static gen3.RobotPlayer.rc;


/*
 # EC Flag
 23      - attack/ defense politician

 */

public class EnlightenmentCenterFlag {
    public static boolean isAttackType (int flag) {
        return (flag & (1<<23)) == (1<<23);
    }

    public static void setDefensePoliticianSpawned() throws GameActionException {
        int flag = rc.getFlag(rc.getID());
        rc.setFlag(flag ^ (1 << 23));
    }

    public static void setAttackPoliticianSpawned() throws GameActionException {
        int flag = rc.getFlag(rc.getID());
        rc.setFlag(flag | (1 << 23));
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {

    }
}
