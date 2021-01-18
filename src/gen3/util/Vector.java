package gen3.util;


public class Vector<T> {
    private final Object[] container;
    public final int maxSize;
    public int length = 0;

    public Vector (T defaultValue, int SIZE){
        container = new Object[SIZE];
        this.maxSize = SIZE;
        for (int j = 0; j < SIZE; j++){
            set(j, defaultValue);
        }
    }

    @SuppressWarnings("unchecked")
    public void set(int i, T value){
        if (i >= this.length){
            throw new ArrayIndexOutOfBoundsException("Vector Size Exceeded");
        }
        container[i] = value;
    }

    public void add(T val) {
        if (length == maxSize) {
            throw new OutOfMemoryError("Insufficient size");
        }
        container[length++] = val;
    }

    @SuppressWarnings("unchecked")
    public T get(int i){
        if (i >= this.length) {
            throw new ArrayIndexOutOfBoundsException("Vector Size Exceeded");
        }
        return (T)container[i];
    }

    public void debug(){
        System.out.println("[");
        for (int i = 0; i < this.length; i++){
            System.out.print(get(i).toString());
            if (i != this.length - 1){
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
}
