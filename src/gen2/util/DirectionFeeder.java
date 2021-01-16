package gen2.util;

import battlecode.common.Direction;

import java.util.ArrayList;

import static gen2.RobotPlayer.rc;

public class DirectionFeeder {
    public final ArrayList<Direction> directions;
    private final boolean recomputePath;

    public DirectionFeeder(ArrayList<Direction> directions, Boolean recomputePath) {
        this.directions = directions;
        this.recomputePath = recomputePath;
    }

    public boolean hasNext() {
        return !directions.isEmpty();
    }

    public Direction getNext() {
        if (!directions.isEmpty()) {
            Direction ret = directions.get(directions.size() - 1);
            // if blockage in path, return null
            if (rc.canMove(ret) || !recomputePath) {
                directions.remove(directions.size() - 1);
                return ret;
            }
        }
        return null;
    }
}
