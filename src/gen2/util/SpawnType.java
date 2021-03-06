package gen2.util;

import static gen2.RobotPlayer.rc;
import static gen2.util.Functions.sigmoid;

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
        return 1 - 0.8*sigmoid((round-300)/25.0);
    }

    private static double getPoliticianProbability (int round) {
        return sigmoid((round-400)/45.0);
    }

    private static double getAttackPoliticianProbability (int round) {
        return sigmoid((round-200)/60.0);
    }

    private static double getSlandererProbability (int round) {
        return sigmoid((round-100)/40.0);
    }
}
