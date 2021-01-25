package gen5.util;

import gen5.EnlightenmentCenter;
import gen5.helpers.SpawnHelper;

import static gen5.RobotPlayer.*;


public enum SpawnType {
    AttackPolitician( 61, 1000),
    DefensePolitician(14, 60),
    GridPolitician( 1, 1),
    Muckraker(1, 5),
    BuffMuckraker(600,600),
    FillerMuckraker(25,25),
    Slanderer(21, 1000);

    public final int minHp, maxHp;

    SpawnType(int minHp, int maxHp) {
        this.minHp = minHp;
        this.maxHp = maxHp;
    }

    private static boolean hasSpawnedBuffed = false;
    private static int muckrakersSpawned = 0;
    public static SpawnType getOptimalType(EcInfo targetEc) {
        if (roundNumber < 4) {
            if (roundNumber == 1) return SpawnType.Slanderer;
            return SpawnType.DefensePolitician;
        } else if (roundNumber < 120) {
            switch (roundNumber % 6) {
                case 1: return SpawnType.DefensePolitician;
                case 3: return SpawnType.Slanderer;
                default : return SpawnType.GridPolitician;
            }
        } else {
            if (haveWonInVotes) {
                return SpawnType.FillerMuckraker;
            }
            if (rc.getConviction() > 1650 && !hasSpawnedBuffed) {
                hasSpawnedBuffed = true;
                return SpawnType.BuffMuckraker;
            }
            if (EnlightenmentCenter.buffMuckApproachDirection != null) {
                return DefensePolitician;
            }
            switch (roundNumber % 11) {
                case 2:
                case 7:
                case 10:
                    if (SpawnHelper.getWeakDirections().length > 0) {
                        return SpawnType.DefensePolitician;
                    }
                    return SpawnType.Slanderer;
                case 3:
                case 6:
                    if (roundNumber < 300) {
                        return SpawnType.Muckraker;
                    }
                    return SpawnType.GridPolitician;
                default:
                    if (targetEc != null && targetEc.enemy) {
                        if (muckrakersSpawned < 10) {
                            muckrakersSpawned++;
                            return SpawnType.Muckraker;
                        }
                    }
                    if (targetEc != null && roundNumber < 600) {
                        return SpawnType.AttackPolitician;
                    }
                    return SpawnType.DefensePolitician;
            }
        }

        //return SpawnType.DefensePolitician;
    }

}