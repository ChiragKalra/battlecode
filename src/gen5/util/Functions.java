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

    public static boolean hasSimilar(Vector<MapLocation> v, MapLocation ml) {
        for (int i = 0; i < v.length; i++) {
            if (ml.isWithinDistanceSquared(v.get(i), 5)) {
                return true;
            }
        }
        return false;
    }

}
