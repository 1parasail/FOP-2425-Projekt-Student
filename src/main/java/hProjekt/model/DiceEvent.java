package hProjekt.model;

import hProjekt.controller.PlayerController;
import java.util.Random;
import java.util.List;

/**
 * Stellt ein zufälliges Ereignis dar, das nach dem Würfeln ausgelöst wird.
 */
public class DiceEvent {
    private static final Random random = new Random();

    /**
     * Wendet einen Bonus oder eine Strafe auf den Spieler an.
     * @param player Der vom Ereignis betroffene Spieler.
     */
    public static void triggerEvent(PlayerController player) {
        List<String> events = List.of(
            "Unwetter: Du verlierst 5 Credits!",
            "Glückspilz: Du bekommst 5 Extra-Credits!"
        );
        String event = events.get(random.nextInt(events.size()));

        applyEvent(player.getPlayer(), event);
        System.out.println("Ereignis: " + event);
    }

    /**
     * Wendet den ausgewählten Ereigniseffekt auf den Player an.
     * @param player Der vom Ereignis betroffene Spieler.
     * @param event Der Ereignis
     */
    private static void applyEvent(Player player, String event) {
        switch (event) {
            case "Unwetter":
                player.removeCredits(5);
                break;
            case "Glückspilz":
                player.addCredits(5);
                break;
        }
    }
}


