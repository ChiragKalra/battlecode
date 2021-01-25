package gen5.util;

import battlecode.common.MapLocation;

public class Functions {

    public static double sigmoid (double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static double gaussian (double x) {
        return Math.exp(-x*x);
    }

    public static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    public static double[] convolveCircularly (double[] array, double[] filter) {
        int sz = array.length, fsz = filter.length;
        double[] out = new double[sz];
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < fsz; j++) {
                out[i] += filter[j] * array[Math.floorMod(j-fsz/2+i, sz)];
            }
        }
        return out;
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
}
