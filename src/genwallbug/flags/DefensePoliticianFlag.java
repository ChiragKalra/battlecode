package gen5.flags;

import battlecode.common.*;
import gen5.helpers.FarmHelper;
import gen5.helpers.MovementHelper;

import static gen5.DefensePolitician.hasChangedEc;
import static gen5.RobotPlayer.*;
import static gen5.DefensePolitician.radius;
import static gen5.helpers.DefenseHelper.*;
import static gen5.util.Functions.getBits;
import static gen5.util.Functions.setBits;

/*
 # Politician Flag
 0		- on wall
 1		- on tunnel point
 2-3:	- quadrant relative to EC
	00	- north-east
	01	- south-east
	10	- south-west
	11	- north-west

19 		- changing EC
20-23   - Buff Muck Direction
 */

public class DefensePoliticianFlag {
    // check for flag changes and set flag
    public static void updateFlag() throws GameActionException {
    	int prevFlag = rc.getFlag(rc.getID());
    	int innerRadius = radius * radius + 1;
        int outerRadius = (radius + 1) * (radius + 1) + 1;
    	int newFlag = onWall(rc.getLocation(), innerRadius, outerRadius) ? 1 : 0;

    	if (isTunnelPoint(rc.getLocation())) {
    		newFlag = setBits(newFlag, 1, 1, 1);
    	} else {
    		newFlag = setBits(newFlag, 3, 2, getQuadrant());
    	}

    	if (hasChangedEc > 0) {
    		hasChangedEc--;
    		newFlag += 1<<19;
		}

    	Direction buffMuck = FarmHelper.directionToBuffMuck();
    	int dirInt = 0;
    	if (buffMuck != null) {
    		dirInt = MovementHelper.directionList.indexOf(buffMuck)+1;
		}
    	newFlag = setBits(newFlag, 23, 20, dirInt);

    	if (prevFlag != newFlag) {
    		rc.setFlag(newFlag);
    	}
    }

    public static boolean hasChangedEc (int flag) {
    	return ((1 << 19) & flag) == (1 << 19);
	}

    public static Direction getBuffMuckrakerDirection (int flag) {
    	int dirInt = getBits(flag, 23, 20);
    	if (dirInt == 0) return null;
    	return MovementHelper.directions[dirInt-1];
	}

    private static int getQuadrant() {
    	if (spawnerLocation == null) {
    		return 0;
    	}
    	MapLocation origin = new MapLocation(spawnerLocation.x + tunnelShift.dx, spawnerLocation.y + tunnelShift.dy);
    	if (rc.getLocation().y > origin.y) {
    		return rc.getLocation().x > origin.x ? 0 : 3;
    	}
    	return rc.getLocation().x > origin.x ? 1 : 2;
    }
}
