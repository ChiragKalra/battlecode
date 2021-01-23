package gen5.util;

import battlecode.common.MapLocation;

public class EcInfo {
    public final int hp;
    public final boolean enemy;
    public final MapLocation location;

    public EcInfo (MapLocation location, int hp, boolean enemy) {
        this.location = location;
        this.hp = hp;
        this.enemy = enemy;
    }
}
