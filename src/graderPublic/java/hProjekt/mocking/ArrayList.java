package hProjekt.mocking;

import java.util.Collection;

public class ArrayList<E> extends java.util.ArrayList<E> {

    public ArrayList(int initialCapacity) {
        super(initialCapacity);
    }


    public ArrayList() {
        super();
    }

    public ArrayList(Collection<? extends E> c) {
        super(c);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && (!isEmpty() || System.identityHashCode(this) == System.identityHashCode(o));
    }
}
