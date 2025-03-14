package hProjekt.model;

import hProjekt.controller.AiController;
import hProjekt.controller.BasicAiController;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;

import java.lang.reflect.Field;

import static hProjekt.TestUtils.newPlayerInstance;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerImplTests {

    @Test
    public void testGetHexGrid() {
        HexGrid hexGrid = new HexGridImpl(new String[] {"City"});
        Player player = newPlayerInstance(hexGrid, Color.BLACK, 0, "Player0", null);
        Context context = contextBuilder().add("player", player).build();

        HexGrid result = callObject(player::getHexGrid);
        assertSame(hexGrid, result, context, r -> "getHexGrid() did not return the corresponding field value");
    }

    @Test
    public void testGetName() {
        String name = "Player0";
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, 0, name, null);
        Context context = contextBuilder().add("player", player).build();

        String result = callObject(player::getName);
        assertSame(name, result, context, r -> "getName() did not return the corresponding field value");
    }

    @Test
    public void testGetId() {
        int id = 0;
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, id, "Player0", null);
        Context context = contextBuilder().add("player", player).build();

        int result = callObject(player::getID);
        assertSame(id, result, context, r -> "getID() did not return the corresponding field value");
    }

    @Test
    public void testGetColor() {
        Color color = Color.BLACK;
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), color, 0, "Player0", null);
        Context context = contextBuilder().add("player", player).build();

        Color result = callObject(player::getColor);
        assertSame(color, result, context, r -> "getColor() did not return the corresponding field value");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testIsAi(boolean ai) {
        Class<? extends AiController> aiController = ai ? BasicAiController.class : null;
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, 0, "Player0", aiController);
        Context context = contextBuilder().add("player", player).build();

        boolean result = callObject(player::isAi);
        assertEquals(ai, result, context, r -> "isAi() did not return the correct value");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 4, 8, 16, 32, 64, 128})
    public void testGetCredits(int credits) throws ReflectiveOperationException {
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, 0, "Player0", null);
        Context context = contextBuilder().add("player", player).build();
        Field creditsField = PlayerImpl.class.getDeclaredField("credits");
        creditsField.setAccessible(true);
        creditsField.set(player, credits);

        int result = callObject(player::getCredits);
        assertEquals(credits, result, context, r -> "getCredits() did not return the corresponding field value");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 4, 8, 16, 32, 64, 128})
    public void testAddCredits(int credits) throws ReflectiveOperationException {
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, 0, "Player0", null);
        Context context = contextBuilder()
            .add("player", player)
            .add("amount", credits)
            .build();
        Field creditsField = PlayerImpl.class.getDeclaredField("credits");
        creditsField.setAccessible(true);
        creditsField.set(player, 16);

        call(() -> player.addCredits(credits));
        assertEquals(credits + 16, creditsField.get(player), context, r ->
            "addCredits(int) did set credits to the correct value");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 4, 8, 16, 32, 64, 128})
    public void testRemoveCredits(int credits) throws ReflectiveOperationException {
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, 0, "Player0", null);
        Context context = contextBuilder()
            .add("player", player)
            .add("amount", credits)
            .build();
        Field creditsField = PlayerImpl.class.getDeclaredField("credits");
        creditsField.setAccessible(true);
        creditsField.set(player, 16);

        boolean result = callObject(() -> player.removeCredits(credits));
        if (16 - credits < 0) {
            assertFalse(result, context, r ->
                "removeCredits(int) did not return false for a parameter value >= credits");
        } else {
            assertTrue(result, context, r ->
                "removeCredits(int) did not return true for a parameter value < credits");
        }
    }

    @Test
    public void testRemoveCreditsNegative() throws ReflectiveOperationException {
        Player player = newPlayerInstance(new HexGridImpl(new String[] {"City"}), Color.BLACK, 0, "Player0", null);
        Context context = contextBuilder()
            .add("player", player)
            .add("amount", -10)
            .build();
        Field creditsField = PlayerImpl.class.getDeclaredField("credits");
        creditsField.setAccessible(true);
        creditsField.set(player, 16);

        boolean result = callObject(() -> player.removeCredits(-10));
        assertFalse(result, context, r -> "removeCredits(int) did not return false for a negative parameter value");
        assertEquals(16, creditsField.get(player), context, r ->
            "removeCredits(int) modified credits but was not supposed to");
    }
}
