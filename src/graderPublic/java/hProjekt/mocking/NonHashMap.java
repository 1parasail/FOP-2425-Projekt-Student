package hProjekt.mocking;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class NonHashMap<K, V> extends java.util.AbstractMap<K, V> {

    private Set<Entry<K,V>>  entries = new ArraySet<>();

    public NonHashMap() {
        super();
    }

    public NonHashMap(Map<? extends K, ? extends V> m) {
        this();
        putAll(m);
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return entries;
    }

    @Override
    public V put(K key, V value) {
        V old = get(key);
        if (old != null) {
            entries.remove(Map.entry(key, old));
        }
        entries.add(Map.entry(key, value));
        return old;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && (!isEmpty() || System.identityHashCode(this) == System.identityHashCode(o));
    }
}
