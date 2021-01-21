package gen5.util;

import static gen5.EnlightenmentCenter.roundCaptured;
import static gen5.RobotPlayer.rc;
import static gen5.util.Functions.sigmoid;

public enum SpawnType {
    AttackPolitician(0, 61, 1000),
    DefensePolitician(0, 16, 60),
    Muckraker(0,1,5),
    Slanderer(0, 21, 1000);

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
            mcd += SpawnType.Muckraker.cooldown;
            return SpawnType.Muckraker;
        } else if (rand < mr + pol) {
            pcd += (int)(SpawnType.DefensePolitician.cooldown * sigmoid((round-50)/50.0));
            return SpawnType.DefensePolitician;
        } else {
            scd += SpawnType.Slanderer.cooldown;
            return SpawnType.Slanderer;
        }
    }

    private static double getMuckrakerProbability (int round) {
        if (round < 75) return 0.9;
        return 0.1;
    }

    private static double getSlandererProbability (int round) {
        if (round < 500) return 0;
        if (roundCaptured == 1) return 0;
        return 0;
    }

    private static double getDefensePoliticianProbability (int round) {
        if (round < 75) return 0.1;
        if (round < 500) return 0.8;
        return 0.5;
    }

}