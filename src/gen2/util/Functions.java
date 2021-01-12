package gen2.util;

public class Functions {

    public static double sigmoid (double x) {
        return 1 / (1 + Math.exp(-x));
    }


    public static Object getRandom(Object[] col) {
        return col[(int) (Math.random() * col.length)];
    }

    public static float[] convolveCircularly (float[] array, float[] filter) {
        int sz = array.length, fsz = filter.length;
        float[] out = new float[sz];
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < fsz; j++) {
                out[i] += filter[j] * array[Math.floorMod(j-fsz/2+i, sz)];
            }
        }
        return out;
    }

}
