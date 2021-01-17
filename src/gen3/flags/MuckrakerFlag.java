package gen3.flags;

import battlecode.common.*;
import gen3.util.Pair;

import java.util.HashMap;

import static gen3.Muckraker.placed;
import static gen3.RobotPlayer.*;
import static gen3.helpers.AttackHelper.getNearbyEC;
import static gen3.helpers.GridHelper.getGridDirectionForFlag;
import static gen3.helpers.MovementHelper.directionList;
import static gen3.helpers.MovementHelper.directions;


/*
 # Muckraker Flag

 0-2,3   - 0-7, 1   -> placed with direction;

           0  , 0   -> searching;
           1  , 0   -> placed without direction;

           2  , 0   -> broadcasting neutral EC coordinates;
           3  , 0   -> broadcasting enemy EC coordinates;
           4  , 0   -> broadcasting enemy EC coordinates for nearby Team EC;
           5  , 0   -> broadcast captured EC coordinates;

           6  , 0   -> free;
           7  , 0   -> free;
 4       - directly observed vacancy

 5       - slanderer adjacent
 6       - broadcast modes muckraker/politician on/off

 7-13    - enemy/neutral enlightenment center x
 14-20   - enemy/neutral enlightenment center y
 21-23   - enlightenment center hp:-
                neutral : ceil((hp-150)/50)
                enemy   : log10(hp)

 */


public class MuckrakerFlag {
    private static final int COOLDOWN_EC_BROADCAST = 10;

    public static boolean isPlaced (int flag) {
        return (flag % 16) == 1 || (flag & 8) == 8;
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

    public static boolean isBroadcastingCaptured(int flag) {
        if ((flag & 8) == 8) {
            return false;
        }
        return flag % 8 == 5;
    }

    public static boolean isBroadcastingEnemyEC(int flag) {
        if ((flag & 8) == 8) {
            return false;
        }
        return flag % 8 == 3;
    }

    public static boolean isBroadcastingEnemyECForTeam (int flag) {
        if ((flag & 8) == 8) {
            return false;
        }
        return flag % 8 == 4;
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

    private static final HashMap<MapLocation, Integer>
            ecsBroadcasts = new HashMap<>(),
            capturedBroadcasts = new HashMap<>();

    // update flag if EC nearby for 10 full rounds
    public static void updateFlagForEC () throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = 0;
        // set enemy/neutral enlightenment center location
        Pair<MapLocation, Integer> got = getNearbyEC(Team.NEUTRAL);
        if (got != null && !ecsBroadcasts.containsKey(got.key)) {
            int relX = got.key.x - spawnerLocation.x + 63,
                    relY = got.key.y - spawnerLocation.y + 63,
                    hp = Math.max((int) Math.ceil((got.value - 150) / 50.0), 0);

            ecsBroadcasts.put(got.key, COOLDOWN_EC_BROADCAST);
            newFlag = 2;
            newFlag += relX << 7;
            newFlag += relY << 14;
            newFlag += hp << 21;
        } else {
            got = getNearbyEC(mTeam.opponent());
            if (got != null && !ecsBroadcasts.containsKey(got.key)) {
                int relX = got.key.x - spawnerLocation.x + 63,
                        relY = got.key.y - spawnerLocation.y + 63,
                        hp = Math.min((int) Math.log10(got.value), 7);

                ecsBroadcasts.put(got.key, COOLDOWN_EC_BROADCAST);
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

    public static boolean updateFlagForECNearby () throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = prevFlag,
                ecFlag = rc.getFlag(enlightenmentCenterId);

        // set enemy/neutral enlightenment center location
        Pair<MapLocation, Integer> got = getNearbyEC(mTeam);
        if (got != null && got.key != spawnerLocation &&
                !ecsBroadcasts.containsKey(got.key) && EnlightenmentCenterFlag.isAttackType(ecFlag)) {
            MapLocation mapLocation = EnlightenmentCenterFlag.getAttackCoordinates(ecFlag);
            int relX = mapLocation.x - got.key.x + 63,
                    relY = mapLocation.y - got.key.y + 63;

            newFlag = 4;
            newFlag += relX << 7;
            newFlag += relY << 14;
        }

        // update
        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
            return true;
        }
        return false;
    }

    public static void updateFlagIfECNearby () throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = prevFlag,
                ecFlag = rc.getFlag(enlightenmentCenterId);

        // set enemy/neutral enlightenment center location
        Pair<MapLocation, Integer> got = getNearbyEC(mTeam);
        if (got != null && got.key != spawnerLocation &&
                !capturedBroadcasts.containsKey(got.key) && EnlightenmentCenterFlag.isAttackType(ecFlag)) {
            int relX = got.key.x - spawnerLocation.x + 63,
                    relY = got.key.y - spawnerLocation.y + 63;

            capturedBroadcasts.put(got.key, COOLDOWN_EC_BROADCAST);
            newFlag = 5;
            newFlag += relX << 7;
            newFlag += relY << 14;
        }

        // update
        if (newFlag != prevFlag) {
            rc.setFlag(newFlag);
        }
    }

    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
        int prevFlag = rc.getFlag(rc.getID()), newFlag = 0;

        // set vacant grid location direction
        if (placed) {
            if (!updateFlagForECNearby()) {
                Pair<Direction, Boolean> direction = getGridDirectionForFlag();
                if (direction == null) {
                    newFlag = 1;
                } else {
                    int threeBit = directionList.indexOf(direction.key);
                    newFlag = threeBit + 8;
                }
            }
        }

        // dont update if EC broadcast displayed
        boolean allowUpdate = true;
        for (MapLocation key: ecsBroadcasts.keySet()) {
            Integer count = ecsBroadcasts.get(key);
            if (count > 0) {
                ecsBroadcasts.put(key, count-1);
                allowUpdate = false;
            }
        }
        if (allowUpdate) {
            for (MapLocation key: capturedBroadcasts.keySet()) {
                Integer count = capturedBroadcasts.get(key);
                if (count > 0) {
                    capturedBroadcasts.put(key, count-1);
                    allowUpdate = false;
                }
            }
        }

        // update
        if (newFlag != prevFlag && allowUpdate) {
            rc.setFlag(newFlag);
        }
    }
}
