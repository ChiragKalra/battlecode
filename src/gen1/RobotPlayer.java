package gen1;

import battlecode.common.*;


public strictfp class RobotPlayer {
    public static final boolean DEBUG = true;

    public static final int MAX_GENERATED_INFLUENCE = 22364;

    public static RobotController rc;
    public static int round;
    public static Team mTeam, enemyTeam;
    public static int actionRadius, sensorRadius, detectionRadius;
    public static RobotType mType;

    public static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    @SuppressWarnings("unused")
    public static void run (RobotController robotController) {
        rc = robotController;
        round = rc.getRoundNum();
        mType = rc.getType();
        mTeam = rc.getTeam();
        enemyTeam = mTeam.opponent();
        actionRadius = rc.getType().actionRadiusSquared;
        sensorRadius = rc.getType().sensorRadiusSquared;
        detectionRadius = rc.getType().detectionRadiusSquared;

        while (round<GameConstants.GAME_MAX_NUMBER_OF_ROUNDS) {
            try {
                // dont compute movement/ability if cooldown active
                if (rc.getCooldownTurns() < 1) {
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

                round++;
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
