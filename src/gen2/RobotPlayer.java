package gen2;

import battlecode.common.*;

import gen2.flags.EnlightenmentCenterFlag;
import gen2.flags.MuckrakerFlag;
import gen2.flags.PoliticianFlag;
import gen2.flags.SlandererFlag;

import static gen2.Muckraker.*;

public strictfp class RobotPlayer {
    // toggle logging before competitive matches
    public static final boolean DEBUG = true;
    public static void log (String s) {
        if (DEBUG) {
            System.out.println(s);
        }
    }


    public static final int MAX_GENERATED_INFLUENCE = 22364;

    public static RobotController rc;
    public static int round;
    public static Team mTeam, enemyTeam;
    public static int actionRadius, sensorRadius, detectionRadius;
    public static RobotType mType;
    public static MapLocation spawnerLocation;
    public static int enlightenmentCenterId;



    private static void setEnlightenmentCenterLocation() {
        if (spawnerLocation == null) {
            if (mType == RobotType.ENLIGHTENMENT_CENTER) {
                spawnerLocation = rc.getLocation();
            } else {
                for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, mTeam)) {
                    if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                        spawnerLocation = gridReferenceLocation = ri.location;
                        enlightenmentCenterId = ri.getID();
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static void run (RobotController robotController) {
        rc = robotController;
        round = rc.getRoundNum();
        mType = rc.getType();
        mTeam = rc.getTeam();
        enemyTeam = mTeam.opponent();
        actionRadius = mType.actionRadiusSquared;
        sensorRadius = mType.sensorRadiusSquared;
        detectionRadius = mType.detectionRadiusSquared;

        if (mType == RobotType.ENLIGHTENMENT_CENTER) {
            log("round ec obtained: " + round);
        }


        try {
            // set spawner location for relative movement
            setEnlightenmentCenterLocation();

            // init special variables
            switch (mType) {
                case ENLIGHTENMENT_CENTER:
                    // EnlightenmentCenter.init();
                    break;
                case POLITICIAN:
                    Politician.init();
                    break;
                case SLANDERER:
                    // Slanderer.init();
                    break;
                case MUCKRAKER:
                    // Muckraker.init();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (round < GameConstants.GAME_MAX_NUMBER_OF_ROUNDS) {
            try {
                // slanderer will convert to politician in 300 rounds, watch for changes
                if (mType != rc.getType()) {
                    mType = rc.getType();
                    detectionRadius = mType.detectionRadiusSquared;
                    actionRadius = mType.actionRadiusSquared;
                    sensorRadius = mType.sensorRadiusSquared;
                }

                // dont compute movement/ability if cooldown active
                if (rc.isReady()) {
                    switch (mType) {
                        case ENLIGHTENMENT_CENTER:
                            EnlightenmentCenter.move();
                            break;
                        case POLITICIAN:
                            Politician.move();
                            break;
                        case SLANDERER:
                            Slanderer.move();
                            break;
                        case MUCKRAKER:
                            Muckraker.move();
                            break;
                    }
                }

                // update flag at the end of each round
                switch (mType) {
                    case ENLIGHTENMENT_CENTER:
                        EnlightenmentCenterFlag.updateFlag();
                        break;
                    case POLITICIAN:
                        PoliticianFlag.updateFlag();
                        break;
                    case SLANDERER:
                        SlandererFlag.updateFlag();
                        break;
                    case MUCKRAKER:
                        MuckrakerFlag.updateFlag();
                }

                if (DEBUG) {
                    float k = 0f;
                    if (Clock.getBytecodesLeft() < 1000 * k) {
                        System.out.println("ByteCodes left under " + k + "k: " + Clock.getBytecodesLeft());
                    }
                }

                round++;
                Clock.yield();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}
