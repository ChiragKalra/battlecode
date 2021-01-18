package gen3.util;

import java.util.*;

public class Vector<T>{
    private final Object[] container;
    public final int SIZE;

    public Vector(T defaultValue, int SIZE){
        container = new Object[SIZE];
        this.SIZE = SIZE;
        for (int j = 0; j < SIZE; j++){
            set(j, defaultValue);
        }
    }

    public void set(int i, T value){
        if (i >= this.SIZE){
            throw new ArrayIndexOutOfBoundsException("Vector Size Exceeded");
        }
        container[i] = value;
    }

    public T get(int i){
        if (i >= this.SIZE){
            throw new ArrayIndexOutOfBoundsException("Vector Size Exceeded");
        }
        T t = (T)container[i];
        return t;
    }

    public void debug(){
        System.out.println("[");
        for (int i = 0; i < this.SIZE; i++){
            System.out.print(get(i).toString());
            if (i != this.SIZE - 1){
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
}
