package gen3.util;

import static gen3.RobotPlayer.rc;
import static gen3.util.Functions.sigmoid;

public enum SpawnType {
    AttackPolitician(4, 21, 500),
    DefensePolitician(4, 21, 200),
    Muckraker(0,1,5),
    Slanderer(2, 21, 200);

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
                aPol = getAttackPoliticianProbability(round),
                slan = getSlandererProbability(round),
                total = mr + pol + slan + aPol,
                rand = Math.random();
        mr /= total;
        pol /= total;
        aPol /= total;
        if (rand < mr) {
            return SpawnType.Muckraker;
        } else if (rand < mr + pol) {
            return SpawnType.DefensePolitician;
        } else if (rand < mr + pol + aPol) {
            return SpawnType.AttackPolitician;
        } else {
            return SpawnType.Slanderer;
        }
    }

    private static double getMuckrakerProbability (int round) {
        return 1 - sigmoid((round-250)/10.0);
    }

    private static double getSlandererProbability (int round) {
        return sigmoid((round-275)/10.0);
    }

    private static double getPoliticianProbability (int round) {
        return sigmoid((round-250)/45.0);
    }

    private static double getAttackPoliticianProbability (int round) {
        return 0*sigmoid((round-200)/60.0);
    }

}
