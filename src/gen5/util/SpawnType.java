package gen5.util;

import battlecode.common.MapLocation;

import static gen5.RobotPlayer.roundNumber;


public enum SpawnType {
    AttackPolitician( 61, 1000),
    DefensePolitician(15, 60),
    GridPolitician( 1, 1),
    Muckraker(1,500),
    Slanderer(21, 1000);

    public final int minHp, maxHp;

    SpawnType(int minHp, int maxHp) {
        this.minHp = minHp;
        this.maxHp = maxHp;
    }

    public static SpawnType getOptimalType(Pair<MapLocation, Integer> targetEc) {
        if (roundNumber < 4) {
            if (roundNumber == 1) return SpawnType.Slanderer;
            return SpawnType.DefensePolitician;
        } else if (roundNumber < 120) {
            switch (roundNumber % 6) {
                case 1: return SpawnType.DefensePolitician;
                case 3: return SpawnType.Slanderer;
                case 5: return SpawnType.GridPolitician;
            }
        } else {
            switch (roundNumber % 11) {
                case 2:
                case 7:
                case 10:
                    return SpawnType.Slanderer;
                case 3:
                case 6:
                    if (targetEc != null) {
                        return SpawnType.Muckraker;
                    }
                    return SpawnType.GridPolitician;
                default:
                    if (targetEc != null) {
                        return SpawnType.AttackPolitician;
                    }
                    return SpawnType.DefensePolitician;
            }
        }

        return SpawnType.DefensePolitician;
    }

}