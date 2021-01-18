package gen3.util;

import static gen3.RobotPlayer.rc;
import static gen3.util.Functions.gaussian;
import static gen3.util.Functions.sigmoid;

public enum SpawnType {
    AttackPolitician(4, 21, 500),
    DefensePolitician(4, 11, 20),
    Muckraker(2,1,2),
    Slanderer(10, 63, 500);

    public final int cooldown, minHp, maxHp;

    SpawnType(int cooldown, int minHp, int maxHp) {
        this.cooldown = cooldown;
        this.minHp = minHp;
        this.maxHp = maxHp;
    }


    private static int mrCd = 0, polCd = 0, aPolCd = 0, slanCd = 0;
    public static SpawnType getOptimalType() {
        int round = rc.getRoundNum();
        if (mrCd > 0) mrCd--;
        if (polCd > 0) polCd--;
        if (aPolCd > 0) aPolCd--;
        if (slanCd > 0) slanCd--;

        double mr = mrCd < 1 ? getMuckrakerProbability(round) : 0,
                pol = polCd < 1 ? getPoliticianProbability(round) : 0,
                aPol = aPolCd < 1 ? getAttackPoliticianProbability(round) : 0,
                slan = slanCd < 1 ? getSlandererProbability(round) : 0,
                total = mr + pol + slan + aPol,
                rand = Math.random();
        mr /= total;
        pol /= total;
        aPol /= total;
        if (rand < mr) {
            mrCd += SpawnType.Muckraker.cooldown;
            return SpawnType.Muckraker;
        } else if (rand < mr + pol) {
            polCd += SpawnType.DefensePolitician.cooldown;
            return SpawnType.DefensePolitician;
        } else if (rand < mr + pol + aPol) {
            aPolCd += SpawnType.AttackPolitician.cooldown;
            return SpawnType.AttackPolitician;
        } else {
            slanCd += SpawnType.Slanderer.cooldown;
            return SpawnType.Slanderer;
        }
    }

    private static double getMuckrakerProbability (int round) {
        return 0.75*gaussian((round-125)/100.0) + 0.25;
    }

    private static double getSlandererProbability (int round) {
        return 1 - gaussian((round-125)/50.0);
    }

    private static double getPoliticianProbability (int round) {
        return 0*sigmoid((round-250)/45.0);
    }

    private static double getAttackPoliticianProbability (int round) {
        return 0*sigmoid((round-200)/60.0);
    }

}
