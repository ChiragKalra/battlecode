package gen1;

import battlecode.common.*;

import static gen1.Muckraker.*;

public strictfp class RobotPlayer {
    // toggle logging before competitive matches
    public static final boolean DEBUG = true;

    public static final int MAX_GENERATED_INFLUENCE = 22364;

    public static RobotController rc;
    public static int round;
    public static Team mTeam, enemyTeam;
    public static int actionRadius, sensorRadius, detectionRadius;
    public static RobotType mType;
    public static MapLocation spawnerLocation;

    public static void log (String s) {
        if (DEBUG) {
            System.out.println(s);
        }
    }

    public static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    private static void setEnlightenmentCenterLocation() {
        if (spawnerLocation == null) {
            if (mType == RobotType.ENLIGHTENMENT_CENTER) {
                spawnerLocation = rc.getLocation();
            } else {
                for (RobotInfo ri : rc.senseNearbyRobots(sensorRadius, mTeam)) {
                    if (ri.type == RobotType.ENLIGHTENMENT_CENTER) {
                        spawnerLocation = gridReferenceLocation = ri.location;
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

        while (round < GameConstants.GAME_MAX_NUMBER_OF_ROUNDS) {
            try {
                // set spawner location for relative movement
                setEnlightenmentCenterLocation();

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
                        EnlightenmentCenter.updateFlag();
                        break;
                    case POLITICIAN:
                        Politician.updateFlag();
                        break;
                    case SLANDERER:
                        Slanderer.updateFlag();
                        break;
                    case MUCKRAKER:
                        Muckraker.updateFlag();
                        break;
                }

                if (DEBUG) {
                    float k = 15f;
                    if (Clock.getBytecodeNum() > 1000*k) {
                        System.out.println("ByteCodes Used over " + k + "k: " + Clock.getBytecodeNum());
                    }
                }

                round++;
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
