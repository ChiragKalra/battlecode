package gen5.flags;

import battlecode.common.*;

import static gen5.RobotPlayer.*;
import static gen5.DefensePolitician.radius;
import static gen5.helpers.DefenseHelper.*;

/*
 # Politician Flag
 0		- on wall
 1		- on tunnel point
 2-3:	- quadrant relative to EC
	00	- north-east
	01	- south-east
	10	- south-west
	11	- north-west
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

    	if (prevFlag != newFlag) {
    		rc.setFlag(newFlag);
    	}
    }

    public static int getBits(int flag, int left, int right) {
    	return (flag >> right) & ((1 << (left - right + 1)) - 1);
    }

    public static int setBits(int flag, int left, int right, int val) {
    	int rightBits = flag & ((1 << right) - 1);
    	flag = ((flag >> (left + 1)) << (left + 1)) | (val << right);
    	flag |= rightBits;
    	return flag;
    }

    private static int getQuadrant() {
    	MapLocation origin = new MapLocation(spawnerLocation.x + tunnelShift.dx, spawnerLocation.y + tunnelShift.dy);
    	if (rc.getLocation().y > origin.y) {
    		return rc.getLocation().x > origin.x ? 0 : 3;
    	}
    	return rc.getLocation().x > origin.x ? 1 : 2;
    }
}
