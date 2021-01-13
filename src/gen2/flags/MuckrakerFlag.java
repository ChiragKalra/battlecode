package gen2.flags;

import battlecode.common.*;
import gen2.util.Pair;

import static gen2.Muckraker.*;
import static gen2.RobotPlayer.*;
import static gen2.helpers.AttackHelper.*;
import static gen2.helpers.GridHelper.*;
import static gen2.helpers.MovementHelper.*;


/*
 # Muckraker Flag

 0-2,3   - 0-7, 1   -> placed with direction;

           0  , 0   -> searching;
           1  , 0   -> placed without direction;

           2  , 0   -> broadcasting neutral EC coordinates;
           3  , 0   -> broadcasting enemy EC coordinates;

           4  , 0   -> free;
           5  , 0   -> free;
           6  , 0   -> free;
           7  , 0   -> free;

 4-6     - politician approaching direction

 7-13    - enemy/neutral enlightenment center x
 14-20   - enemy/neutral enlightenment center y
 21-23   - enlightenment center hp:-
                neutral : ceil((hp-150)/50)
                enemy   : log10(hp)

 */
public class MuckrakerFlag {
    public static boolean isPlaced (int flag) {
        return (flag % 16) != 0;
    }

    public static Direction getDirection(int flag) {
        if ((flag & 8) != 8) {
            return null;
        }
        return directions[flag % 8];
    }

    public static boolean isBroadcastingNeutralEC(int flag) {
        if ((flag & 8) == 8) {
            return false;
        }
        return flag % 8 == 2;
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

    public static int getEnemyHpFromFlag(int flag) {
        return (int) Math.pow((flag >> 21) % 8, 10);
    }


    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = 0;

        // set vacant grid location direction
        if (placed) {
            Direction direction = getGridDirectionForFlag();
            if (direction == null) {
                newFlag = 1;
            } else {
                int threeBit = directionList.indexOf(direction);
                newFlag = threeBit + 8;
            }
        }

        // set enemy/neutral enlightenment center location
        Pair<MapLocation, Integer> got = getNearbyNeutralEC();
        if (spawnerLocation != null && got != null) {
            int relX = got.key.x - spawnerLocation.x + 63,
                    relY = got.key.y - spawnerLocation.y + 63,
                    hp = (int) Math.ceil((got.value-150)/50.0);

            newFlag = 2;
            newFlag += relX << 7;
            newFlag += relY << 14;
            newFlag += hp << 21;
        }
        if (got == null) {
            got = getNearbyEnemyEC();
            if (spawnerLocation != null && got != null) {
                int relX = got.key.x - spawnerLocation.x + 63,
                        relY = got.key.y - spawnerLocation.y + 63,
                        hp = Math.min((int) Math.log10(got.value), 7);

                newFlag = 3;
                newFlag += relX << 7;
                newFlag += relY << 14;
                newFlag += hp << 21;
            }
        }

        // update
        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }
    }

}
