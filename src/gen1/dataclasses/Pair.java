package gen1.dataclasses;

public class Pair<T, U> {
    public T key;
    public U value;
    
    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }
}
