package gen3;

import battlecode.common.*;

import gen3.flags.EnlightenmentCenterFlag;
import gen3.flags.PoliticianFlag;
import gen3.flags.SlandererFlag;
import gen3.flags.MuckrakerFlag;
import gen3.util.Logger;

import static gen3.Politician.isAttackType;

public strictfp class RobotPlayer {
    // toggle logging before competitive matches
    public static final boolean DEBUG = true;
    public static void log (String s) {
        if (DEBUG) {
            System.out.println(s);
        }
    }

    public static RobotController rc;
    public static Team mTeam, enemyTeam;
    public static int actionRadius, sensorRadius, detectionRadius;
    public static RobotType mType;
    public static MapLocation spawnerLocation;
    public static int enlightenmentCenterId, roundNumber;



    private static void setEnlightenmentCenterLocation() {
        if (spawnerLocation == null) {
            if (mType == RobotType.ENLIGHTENMENT_CENTER) {
                spawnerLocation = rc.getLocation();
            } else {
                for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, mTeam)) {
                    if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                        spawnerLocation = ri.location;
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
        mType = rc.getType();
        mTeam = rc.getTeam();
        enemyTeam = mTeam.opponent();
        actionRadius = mType.actionRadiusSquared;
        sensorRadius = mType.sensorRadiusSquared;
        detectionRadius = mType.detectionRadiusSquared;

        if (mType == RobotType.ENLIGHTENMENT_CENTER) {
            log("round ec obtained: " + rc.getRoundNum());
        }


        try {
            // set spawner location for relative movement
            setEnlightenmentCenterLocation();

            // init special variables
            switch (mType) {
                case ENLIGHTENMENT_CENTER:
                    EnlightenmentCenter.init();
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

        while (rc.getRoundNum() <= GameConstants.GAME_MAX_NUMBER_OF_ROUNDS) {
            try {
                roundNumber = rc.getRoundNum();
                Logger logger = new Logger("full", true);

                // slanderer will convert to politician in 300 rounds, watch for changes
                if (mType != rc.getType()) {
                    mType = rc.getType();
                    // Decide weather newly formed pol is defense politician or attack politician
                    isAttackType = true;
                    detectionRadius = mType.detectionRadiusSquared;
                    actionRadius = mType.actionRadiusSquared;
                    sensorRadius = mType.sensorRadiusSquared;
                }

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
                        if (rc.isReady()) {
                            Muckraker.move();
                        }
                        break;
                }
                logger.log("move");

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

                logger.flush();


                if (roundNumber == rc.getRoundNum()) {
                    Clock.yield();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        //
    }
}
