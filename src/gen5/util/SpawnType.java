package gen5.util;

import static gen5.RobotPlayer.rc;
import static gen5.RobotPlayer.roundNumber;


public enum SpawnType {
    AttackPolitician( 61, 1000),
    DefensePolitician(18, 60),
    GridPolitician( 1, 1),
    Muckraker(1, 5),
    BuffMuckraker(1000,1000),
    Slanderer(21, 1000);

    public final int minHp, maxHp;

    SpawnType(int minHp, int maxHp) {
        this.minHp = minHp;
        this.maxHp = maxHp;
    }

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
            switch (roundNumber % 11) {
                case 2:
                case 7:
                case 10:
                    return SpawnType.Slanderer;
                case 3:
                case 6:
                    return SpawnType.GridPolitician;
                default:
                    if (targetEc != null && roundNumber < 750) {
                        return SpawnType.AttackPolitician;
                    }
                    if (targetEc != null && targetEc.enemy && Math.random() > 0.5) {
                        if (rc.getConviction() > 2000 && Math.random() < 0.005) {
                            return SpawnType.BuffMuckraker;
                        } else {
                            return SpawnType.Muckraker;
                        }
                    }
                    return SpawnType.DefensePolitician;
            }
        }

        //return SpawnType.DefensePolitician;
    }

}