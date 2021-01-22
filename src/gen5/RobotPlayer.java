package gen5;

import battlecode.common.*;

import gen5.flags.EnlightenmentCenterFlag;
import gen5.flags.DefensePoliticianFlag;
import gen5.flags.GridPoliticianFlag;
import gen5.util.Logger;
import gen5.util.SpawnType;

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
    public static SpawnType spawnType;
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

        int hp = rc.getConviction();
        if (mType == RobotType.POLITICIAN) {
            if ( hp <= SpawnType.GridPolitician.maxHp) {
                spawnType = SpawnType.GridPolitician;
            } else if ( hp <= SpawnType.DefensePolitician.maxHp) {
                spawnType = SpawnType.DefensePolitician;
            } else  {
                spawnType = SpawnType.AttackPolitician;
            }
        } else if (mType == RobotType.MUCKRAKER) {
            if (hp <= SpawnType.Muckraker.maxHp) {
                spawnType = SpawnType.Muckraker;
            } else {
                spawnType = SpawnType.BuffMuckraker;
            }
        } else if (mType == RobotType.SLANDERER) {
            spawnType = SpawnType.Slanderer;
        }

        try {
            // set spawner location for relative movement
            setEnlightenmentCenterLocation();

            if (spawnType == null) {
                EnlightenmentCenter.init();
            } else switch (spawnType) {
                case DefensePolitician:
                    DefensePolitician.init();
                    break;
                case Slanderer:
                    Slanderer.init();
                    break;
                case Muckraker:
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
                    detectionRadius = mType.detectionRadiusSquared;
                    actionRadius = mType.actionRadiusSquared;
                    sensorRadius = mType.sensorRadiusSquared;

                    hp = rc.getConviction();
                    if ( hp <= SpawnType.DefensePolitician.maxHp) {
                        spawnType = SpawnType.DefensePolitician;
                    } else  {
                        spawnType = SpawnType.AttackPolitician;
                    }
                }

                if (spawnType == null) {
                    EnlightenmentCenter.move();
                } else switch (spawnType) {
                    case AttackPolitician:
                        if (rc.isReady()) {
                            AttackPolitician.move();
                        }
                        break;
                    case DefensePolitician:
                        if (rc.isReady()) {
                            DefensePolitician.move();
                        }
                        break;
                    case GridPolitician:
                        if (rc.isReady()) {
                            GridPolitician.move();
                        }
                        break;
                    case Muckraker:
                        //Muckraker.move();
                    case Slanderer:
                        Slanderer.move();
                        break;
                }
                logger.log("move");

                // update flag at the end of each round
                if (spawnType == null) {
                    EnlightenmentCenterFlag.updateFlag();
                } else switch (spawnType) {
                    case DefensePolitician:
                        DefensePoliticianFlag.updateFlag();
                        break;
                    case GridPolitician:
                        GridPoliticianFlag.updateFlag();
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
