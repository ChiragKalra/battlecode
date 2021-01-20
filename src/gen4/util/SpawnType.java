package gen4.util;

import static gen4.EnlightenmentCenter.roundCaptured;
import static gen4.RobotPlayer.rc;

public enum SpawnType {
    AttackPolitician(0, 26, 10000),
    DefensePolitician(0, 14, 25),
    Muckraker(0,1,5),
    Slanderer(0, 41, 10000);

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
        if (total == 0) {
            return null;
        }
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
        if (round < 75) return 0.9;
        if (round < 250 && round > 200) return 0;
        return 0.05;
    }

    private static double getSlandererProbability (int round) {
        if (round < 150) return 0;
        if (round < 225 && round > 200) return 0;
        return 0.1;
    }

    private static double getDefensePoliticianProbability (int round) {
        if (round < 75) return 0.1;
        if (round < 200) return 1;
        if (round < 250 && round > 200) return 0;
        if (round < 400) return 0.3;
        return 0.5;
    }

}