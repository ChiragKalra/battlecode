package gen3.util;

import battlecode.common.Clock;

import static gen3.RobotPlayer.*;

public class Logger {
    private final int start, startRN;
    private int prev, prevRN;
    private final StringBuilder pre = new StringBuilder(), output = new StringBuilder();
    private int totalLogs = 0;

    // LogCounts is total number of logs for a class including total.
    public Logger (String title) {
        start = prev = Clock.getBytecodeNum();
        startRN = prevRN = rc.getRoundNum();
        pre.append("Log_Start@@").append(prevRN).append(" ");
        output.append(" ").append(title).append('\n');
    }

    public void log (String event) {
        totalLogs++;
        output.append(event).append(" ")
                .append(Clock.getBytecodeNum()-prev + (rc.getRoundNum()-prevRN)*mType.bytecodeLimit).append('\n');
        prev = Clock.getBytecodeNum();
        prevRN = rc.getRoundNum();
    }

    public void flush () {
        if (DEBUG) {
            log("end");
            output.append("total ")
                    .append(Clock.getBytecodeNum()-start + (rc.getRoundNum()-startRN)*mType.bytecodeLimit);
            pre.append(totalLogs).append(output.toString());
            System.out.println(pre.toString());
        }
    }
}
