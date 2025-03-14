package hProjekt.mocking;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

public class ArraySet<E> extends AbstractSet<E> {
    private final ArrayList<E> list = new ArrayList<>();

    @Override
    public boolean add(E e) {
        if (!list.contains(e)) {
            list.add(e);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean contains(Object o) {
        for (E elem: list) {
            if (elem.equals(o)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && (!isEmpty() || System.identityHashCode(this) == System.identityHashCode(o));
    }
}