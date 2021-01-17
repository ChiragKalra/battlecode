package gen3.util;

public class Pair<T, U> {
    public T key;
    public U value;
    
    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals (Object a) {
        if (a == null) return false;
        if (!(a instanceof Pair)) return false;
        Pair<?, ?> p = (Pair<?, ?>) a;
        return key.equals(p.key) && value.equals(p.value);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
