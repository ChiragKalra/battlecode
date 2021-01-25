package gen5.flags;

import battlecode.common.*;
import gen5.util.EcInfo;
import gen5.util.Logger;

import static gen5.GridPolitician.placed;
import static gen5.RobotPlayer.*;
import static gen5.helpers.GridHelper.*;
import static gen5.helpers.MovementHelper.directionList;
import static gen5.helpers.MovementHelper.directions;


/*
 # Grid Flag

 0       - placed;
 1-3     - adjacent vacancy direction
 4       - broadcasting EC
 5-9     - ec near grid x
 10-14   - ec near grid y
 15-22   - ec hp
 23      - enemy EC
 */

public class GridPoliticianFlag {
    public static final int HP_LOSS_RATIO = 20;

    public static boolean isPlaced (int flag) {
        return (flag & 1) == 1;
    }

    public static Direction getDirection (int flag) {
        return directions[(flag>>1) % 8];
    }

    public static boolean isBroadcastingEC (int flag) {
        return (flag & 16) == 16;
    }

    public static MapLocation getAbsLocFromFlag (int flag, MapLocation muck) {
        int relX = (flag >> 5) % 32 - 13,
                relY = (flag >> 10) % 32 - 13;
        return new MapLocation((relX-1)*5 + muck.x+3, (relY-1)*5 + muck.y + 3);
    }

    public static MapLocation getRelLocFromFlag (int flag) {
        int relX = (flag >> 5) % 32 - 13,
                relY = (flag >> 10) % 32 - 13;
        return new MapLocation(relX, relY);
    }

    public static int getHpFromFlag (int flag) {
        return ((flag<<1) >> 16)*HP_LOSS_RATIO - HP_LOSS_RATIO;
    }

    public static boolean isEnemyEc (int flag) {
        return (flag & (1<<23)) > 0;
    }

    private static int getFlag(EcInfo got) {
        //log("location="+got.location+",hp="+got.hp+",enemy="+got.enemy);
        int newFlag = 1<<4;
            int relX = (got.location.x+13) << 5,
                    relY = (got.location.y+13) << 10,
                    hp = Math.min(255, (int)Math.ceil((got.hp+HP_LOSS_RATIO)/(double)HP_LOSS_RATIO));
            newFlag += relX + relY + (hp << 15) + (got.enemy ? (1<<23) : 0);
        return newFlag;
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = 0;

        // set vacant grid location direction
        if (placed) {
            newFlag += 1;
            Direction direction = getGridDirectionForFlag(rc.senseNearbyRobots(sensorRadius, mTeam));
            if (direction != null) {
                int threeBit = directionList.indexOf(direction);
                newFlag += threeBit<<1;
            }

            EcInfo got = getNearbyEC();
            if (got != null) {
                newFlag += getFlag(got);
            } else {
                got = getECFromAdjFlags();
                if (got != null) {
                    newFlag += getFlag(got);
                }
            }
        } else {
            EcInfo got = getNearbyEC();
            if (got != null) {
                newFlag += getFlag(got);
            }
        }

        // update
        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }
    }
}
