package hProjekt;

import java.util.function.Supplier;

public class Dice implements Supplier<Integer> {
    @Override
    public Integer get() {
        return 2;
    }
}
