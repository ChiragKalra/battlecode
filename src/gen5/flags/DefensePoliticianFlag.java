package gen5.flags;

import battlecode.common.*;
import gen5.helpers.FarmHelper;
import gen5.helpers.MovementHelper;

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

29 		- changing EC
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

    	Direction buffMuck = FarmHelper.directionToBuffMuck();
    	int dirInt = 8;
    	if (buffMuck != null) {
    		dirInt = MovementHelper.directionList.indexOf(buffMuck);
		}
    	newFlag = setBits(newFlag, 23, 20, dirInt);

    	if (prevFlag != newFlag) {
    		rc.setFlag(newFlag);
    	}
    }

    public static Direction getBuffMuckrakerDirection (int flag) {
    	int dirInt = getBits(flag, 23, 20);
    	if (dirInt == 8) return null;
    	return MovementHelper.directions[dirInt];
	}

    private static int getQuadrant() {
    	MapLocation origin = new MapLocation(spawnerLocation.x + tunnelShift.dx, spawnerLocation.y + tunnelShift.dy);
    	if (rc.getLocation().y > origin.y) {
    		return rc.getLocation().x > origin.x ? 0 : 3;
    	}
    	return rc.getLocation().x > origin.x ? 1 : 2;
    }
}
