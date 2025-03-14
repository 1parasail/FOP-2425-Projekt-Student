package hProjekt;

import java.util.function.Supplier;

import hProjekt.controller.GameController;
import hProjekt.model.GameState;

public class TestGameController extends GameController {

    public TestGameController(GameState state, Supplier<Integer> dice) {
        super(state, dice);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameController gc)) {
            return false;
        }
        return this.getState().equals(gc.getState());
    }

    @Override
    public int hashCode() {
        return this.getState().hashCode();
    }
}
