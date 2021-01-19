package gen4.util;

import static gen4.EnlightenmentCenter.roundCaptured;
import static gen4.RobotPlayer.rc;

public enum SpawnType {
    AttackPolitician(0, 26, 5000),
    DefensePolitician(2, 16, 25),
    Muckraker(0,1,5),
    Slanderer(0, 21, 5000);

    public final int cooldown, minHp, maxHp;

    SpawnType(int cooldown, int minHp, int maxHp) {
        this.cooldown = cooldown;
        this.minHp = minHp;
        this.maxHp = maxHp;
    }

    private static int mcd = 0, pcd = 0, scd = 0;
    public static SpawnType getOptimalType() {
        if (mcd>0) mcd--;
        if (pcd>0) pcd--;
        if (scd>0) scd--;

        int round = rc.getRoundNum() - roundCaptured;
        double mr = mcd > 0 ? 0 : getMuckrakerProbability(round),
                pol = pcd > 0 ? 0 : getDefensePoliticianProbability(round),
                slan = scd > 0 ? 0 : getSlandererProbability(round),
                total = mr + pol + slan,
                rand = Math.random();
        mr /= total;
        pol /= total;
        if (rand < mr) {
            mcd++;
            return SpawnType.Muckraker;
        } else if (rand < mr + pol) {
            pcd++;
            return SpawnType.DefensePolitician;
        } else {
            scd++;
            return SpawnType.Slanderer;
        }
    }

    private static double getMuckrakerProbability (int round) {
        if (round < 75) return 1;
        return 0;
    }

    private static double getSlandererProbability (int round) {
        if (round < 150) return 0;
        return 0.5;
    }

    private static double getDefensePoliticianProbability (int round) {
        if (round < 75) return 0.1;
        if (round < 500) return 1;
        return 0.5;
    }

}