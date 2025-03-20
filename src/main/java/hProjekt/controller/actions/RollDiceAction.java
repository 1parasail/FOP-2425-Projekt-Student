package hProjekt.controller.actions;

import hProjekt.controller.PlayerController;
import hProjekt.model.DiceEvent;

/**
 * An action to roll the dice.
 */
public class RollDiceAction implements PlayerAction {

    @Override
    public void execute(PlayerController pc) throws IllegalActionException {
        pc.rollDice();

        if (Math.random() <= 0.1) {
            DiceEvent.triggerEvent(pc);
        }
    }
}
