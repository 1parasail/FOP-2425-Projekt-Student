package hProjekt.mocking;

import org.checkerframework.checker.units.qual.K;

import java.util.Map;

public class HashMap<K, V> extends java.util.HashMap<K, V> {

    public HashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public HashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public HashMap() {
        super();
    }

    public HashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && (!isEmpty() || System.identityHashCode(this) == System.identityHashCode(o));
    }
}
