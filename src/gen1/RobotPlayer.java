package gen1;

import battlecode.common.*;


@SuppressWarnings("unused")
public strictfp class RobotPlayer {
    public static final boolean DEBUG = true;

    public static final int MAX_GENERATED_INFLUENCE = 22364;

    public static RobotController rc;
    public static int round;
    public static Team mTeam, enemyTeam;
    public static int actionRadius, sensorRadius, detectionRadius;

    public static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    public static void run (RobotController robotController) {
        rc = robotController;
        round = rc.getRoundNum();
        mTeam = rc.getTeam();
        enemyTeam = mTeam.opponent();
        actionRadius = rc.getType().actionRadiusSquared;
        sensorRadius = rc.getType().sensorRadiusSquared;
        detectionRadius = rc.getType().detectionRadiusSquared;

        while (true) {
            round++;
            try {
                switch (rc.getType()) {
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
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
