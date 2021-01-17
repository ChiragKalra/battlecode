package gen3.util;

import battlecode.common.Clock;

import static gen3.RobotPlayer.*;

public class Logger {
    private final int start, startRN;
    private int prev, prevRN;
    private final StringBuilder output = new StringBuilder();

    public Logger (String title) {
        start = prev = Clock.getBytecodeNum();
        startRN = prevRN = rc.getRoundNum();
        output.append(title).append(" Log Start@").append(prevRN).append('\n');
    }

    public void log (String event) {
        output.append(event).append('=')
                .append(Clock.getBytecodeNum()-prev + (rc.getRoundNum()-prevRN)*mType.bytecodeLimit).append('\n');
        prev = Clock.getBytecodeNum();
        prevRN = rc.getRoundNum();
    }

    public void flush () {
        if (DEBUG) {
            log("end");
            output.append("-----total=")
                    .append(Clock.getBytecodeNum()-start + (rc.getRoundNum()-startRN)*mType.bytecodeLimit);
            System.out.println(output.toString());
        }
    }
}
