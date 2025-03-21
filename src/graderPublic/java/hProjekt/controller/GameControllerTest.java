package hProjekt.controller;

import static hProjekt.Project_TestP.assertContainsAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertTrue;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;

import com.fasterxml.jackson.databind.node.ObjectNode;

import hProjekt.Config;
import hProjekt.Project_TestP;
import hProjekt.mocking.MockConverterP;
import hProjekt.mocking.ReflectionUtilsP;
import hProjekt.mocking.StudentMethodCall;

@TestForSubmission
public class GameControllerTest {

    @ParameterizedTest
    @MethodSource("provideChooseCities")
    public void testChooseCities(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.GameController.class.getDeclaredMethod("chooseCities");
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        Runnable beforeEach = () -> Config.RANDOM.setSeed(0);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node, beforeEach);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }

        Throwable lastCall = null;
        for (StudentMethodCall actual : results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                        .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                        .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                        .add("actual", actual.invoked).add(
                                "expected", expected)
                        .build();

                assertTrue(actual.invoked instanceof GameController, context,
                        r -> "chooseCities() did not return a object of the correct Type!");

                Context context_2 = contextBuilder()
                        .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                        .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                        .add("actual", ReflectionUtilsP.getFieldValue(actual.invoked,
                                "chosenCitiesProperty"))
                        .add(
                                "expected", ReflectionUtilsP.getFieldValue(expected,
                                        "chosenCitiesProperty"))
                        .build();

                assertTrue(
                        ReflectionUtilsP.equalsForMocks(
                                ReflectionUtilsP.getFieldValue(expected, "chosenCitiesProperty"),
                                ReflectionUtilsP.getFieldValue(actual.invoked, "chosenCitiesProperty")),
                        context_2, r -> "chooseCities() did not set chosenCitiesProperty correctly!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideChooseCities() {
        return Project_TestP.parseJsonFile("hProjekt/controller/GameController_chooseCities.json");
    }

    @ParameterizedTest
    @MethodSource("provideExecuteBuildingPhase")
    public void testExecuteBuildingPhase(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.GameController.class.getDeclaredMethod("executeBuildingPhase");
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        Runnable beforeEach = () -> Config.UNCONNECTED_CITIES_START_THRESHOLD = 0;
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node, beforeEach);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }

        Throwable lastCall = null;
        for (StudentMethodCall actual : results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                        .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                        .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                        .build();

                assertTrue(actual.invoked instanceof GameController, context,
                        r -> "chooseCities() did not return a object of the correct Type!");
                GameController expectedGameController = ((GameController) (expected));
                GameController actualGameController = ((GameController) (actual.invoked));
                actualGameController.getState().getGrid().getEdges().entrySet().forEach(edges -> {
                    assertTrue(!edges.getValue().getRailOwners().isEmpty(), context,
                            r -> "GameController did not call waitForBuild() correctly");
                });
                assertEquals(expectedGameController.roundCounterProperty().get(),
                        actualGameController.roundCounterProperty().get(), context,
                        r -> "roundCounter does not match expected.");
                verify(actualGameController, atLeast(1)).withActivePlayer(any(), any());
                verify(actualGameController, atLeast(1)).castDice();
                actualGameController.getPlayerControllers().values().forEach(pc -> {
                    verify(pc, atLeast(1)).setBuildingBudget(2);
                });
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideExecuteBuildingPhase() {
        return Project_TestP.parseJsonFile("hProjekt/controller/GameController_executeBuildingPhase.json");
    }

    @ParameterizedTest
    @MethodSource("provideLetPlayersChoosePath")
    public void testLetPlayersChoosePath(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.GameController.class.getDeclaredMethod("letPlayersChoosePath");
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }

        Throwable lastCall = null;
        for (StudentMethodCall actual : results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                        .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                        .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                        .build();

                assertTrue(actual.invoked instanceof GameController, context,
                        r -> "letPlayersChoosePath() did not return a object of the correct Type!");
                GameController expectedGameController = ((GameController) (expected));
                GameController actualGameController = ((GameController) (actual.invoked));
                actualGameController.getPlayerControllers().values().forEach(apc -> {
                    boolean actualMatchesExpected = expectedGameController.getPlayerControllers().values().stream()
                            .anyMatch(epc -> ReflectionUtilsP.equalsForMocks(apc, epc));
                    assertTrue(actualMatchesExpected, context,
                            r -> ("Player Controller " + apc) + " does not match any expected Player controller!");
                });
                assertContainsAll(((GameController) (expected)).getState().getPlayerPositions(),
                        actualGameController.getState().getPlayerPositions(), context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideLetPlayersChoosePath() {
        return Project_TestP.parseJsonFile("hProjekt/controller/GameController_letPlayersChoosePath.json");
    }

    @ParameterizedTest
    @MethodSource("provideGetWinners")
    public void testGetWinners(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.GameController.class.getDeclaredMethod("getWinners");
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        Runnable beforeEach = () -> Config.WINNING_CREDITS = List.of(10, 5, 1);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node, beforeEach);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }

        Throwable lastCall = null;
        for (StudentMethodCall actual : results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                        .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                        .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                        .build();

                assertContainsAll((List<Object>) expected, (List<Object>) actual.call.returnValue(), context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetWinners() {
        return Project_TestP.parseJsonFile("hProjekt/controller/GameController_getWinners.json");
    }

}
