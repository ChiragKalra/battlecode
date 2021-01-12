package gen2.helpers;

import battlecode.common.*;
import gen2.util.PassabilityGrid;

import static gen2.helpers.MovementHelper.*;
import static gen2.RobotPlayer.*;

public class SpawnHelper {

    public static Direction getOptimalDirection (Direction to) throws GameActionException {
        MapLocation current = rc.getLocation();
        PassabilityGrid grid = new PassabilityGrid(current, sensorRadius);
        float[] filter = {.5f, .25f, .125f, .0625f, 0.03125f},
                identity = {1f, 1f, 1f, 1f, 1f};

        int dirInd = directionList.indexOf(to), decided = dirInd;

        if (to == null) {
            filter = identity;
            dirInd = 0;
            decided = 0;
        }

        double factor = 0;

        for (int i = 0; i < 8; i++) {
            Direction d = directions[i];
            int filterInd = Math.min(Math.abs(dirInd-i), Math.abs(dirInd - 8 + i));
            double cur = filter[filterInd]*grid.getRelative(d.dx, d.dy);
            if (cur > factor) {
                decided = i;
                factor = cur;
            }
        }

        return directions[decided];
    }

}
