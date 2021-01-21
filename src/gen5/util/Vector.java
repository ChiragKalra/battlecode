package gen5.util;

import static gen5.RobotPlayer.log;

@SuppressWarnings("unchecked")
public class Vector<T> {
    private final T[] container;
    public final int maxSize;
    public int length = 0;

    public Vector (T defaultValue, int SIZE){
        container = (T[]) new Object[SIZE];
        this.maxSize = SIZE;
        if (defaultValue != null) {
            for (int j = 0; j < SIZE; j++) {
                set(j, defaultValue);
            }
        }
    }

    public void set(int i, T value){
        if (i >= this.length){
            log("Vector Size Exceeded");
        } else {
            container[i] = value;
        }
    }

    public void add(T val) {
        if (length == maxSize) {
            log("Insufficient size");
        } else {
            container[length++] = val;
        }
    }

    public T get(int i){
        if (i >= this.length) {
            log("Vector Size Exceeded");
            return null;
        } else {
            return container[i];
        }
    }

    public boolean has (T a) {
        for (int i = 0; i < length; i++) {
            if (a.equals(container[i])) {
                return true;
            }
        }
        return false;
    }
}
