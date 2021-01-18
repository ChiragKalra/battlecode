package gen3.util;

import static gen3.RobotPlayer.rc;
import static gen3.util.Functions.gaussian;
import static gen3.util.Functions.sigmoid;

public enum SpawnType {
    //AttackPolitician(0, 21, 1000),
    DefensePolitician(1, 12, 50),
    Muckraker(0,1,20),
    Slanderer(2, 41, 5000);

    public final int cooldown, minHp, maxHp;

    SpawnType(int cooldown, int minHp, int maxHp) {
        this.cooldown = cooldown;
        this.minHp = minHp;
        this.maxHp = maxHp;
    }

    public static SpawnType getOptimalType() {
        int round = rc.getRoundNum();
        double mr = getMuckrakerProbability(round),
                pol = getPoliticianProbability(round),
                slan = getSlandererProbability(round),
                total = mr + pol + slan,
                rand = Math.random();
        mr /= total;
        pol /= total;
        if (rand < mr) {
            return SpawnType.Muckraker;
        } else if (rand < mr + pol) {
            return SpawnType.DefensePolitician;
        } else {
            return SpawnType.Slanderer;
        }
    }

    private static double getMuckrakerProbability (int round) {
        return 0.75*gaussian((round-150)/35.0) + 0.25;
    }

    private static double getSlandererProbability (int round) {
        return sigmoid((round-75)/50.0);
    }

    private static double getPoliticianProbability (int round) {
        return 0.5*sigmoid((round-1250)/100.0) + 0.2;
    }

}