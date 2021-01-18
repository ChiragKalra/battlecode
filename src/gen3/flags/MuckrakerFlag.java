package gen3.flags;

import battlecode.common.*;
import gen3.util.Pair;

import static gen3.Muckraker.placed;
import static gen3.RobotPlayer.*;
import static gen3.helpers.GridHelper.*;
import static gen3.helpers.MovementHelper.directionList;
import static gen3.helpers.MovementHelper.directions;


/*
 # Muckraker Flag

 0       - placed;
 1-3     - adjacent vacancy direction
 4       - broadcasting EC
 5-9     - ec near grid x
 10-14   - ec near grid y
 15-23   - ec hp
 */

public class MuckrakerFlag {
    public static final int HP_LOSS_RATIO = 10;

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

    public static Pair<Integer, Integer> getRelLocFromFlag (int flag) {
        int relX = (flag >> 5) % 32 - 13,
                relY = (flag >> 10) % 32 - 13;
        return new Pair<>(relX, relY);
    }

    public static int getHpFromFlag (int flag) {
        return (flag >> 15)*HP_LOSS_RATIO - HP_LOSS_RATIO;
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = 0;

        // set vacant grid location direction
        if (placed) {
            newFlag += 1;
            Pair<Direction, Boolean> direction = getGridDirectionForFlag();
            if (direction != null) {
                int threeBit = directionList.indexOf(direction.key);
                newFlag += threeBit<<1;
            }

            Pair<Pair<Integer, Integer>, Integer> got = getNearbyEC();
            if (got != null) {
                newFlag += 1<<4;
                int relX = (got.key.key+13) << 5,
                        relY = (got.key.value+13) << 10,
                        hp = Math.min(511, (int)Math.ceil((got.value+HP_LOSS_RATIO)/(double)HP_LOSS_RATIO));
                newFlag += relX + relY + (hp << 15);
                //log("yeah"+got.key.key+','+got.key.value+'='+got.value);
            } else {
                got = getECFromAdjFlags();
                if (got != null ) {
                    //log("yeah"+got.key.key+','+got.key.value+'='+got.value);
                    newFlag += 1 << 4;
                    int relX = (got.key.key + 13) << 5,
                            relY = (got.key.value + 13) << 10,
                            hp = Math.min(511, (int) Math.ceil((got.value + HP_LOSS_RATIO) / (double) HP_LOSS_RATIO));
                    newFlag += relX + relY + (hp << 15);
                }
            }
        }

        // update
        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }
    }
}
