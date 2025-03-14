package hProjekt.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import hProjekt.Project_TestP;
import hProjekt.mocking.MockConverterP;
import hProjekt.mocking.ReflectionUtilsP;
import hProjekt.mocking.StudentMethodCall;
import hProjekt.model.EdgeImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Context;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static hProjekt.Project_TestP.assertSetEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertNotNull;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;

@TestForSubmission
public class PlayerControllerTest {

    @ParameterizedTest
    @MethodSource("provideBuildRail")
    public void testBuildRail(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.PlayerController.class.getDeclaredMethod("buildRail", hProjekt.model.Edge.class);
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node);

        Throwable lastCall = null;
        for (StudentMethodCall actual: results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                    .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                    .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                    .add("Exception class", actual.exception != null ? actual.exception.getClass() : "none")
                    .add("Exception message", actual.exception != null ? actual.exception.getMessage(): "none")
                    .add("Exception stacktrace", actual.exception != null ? ReflectionUtilsP.formatStackTrace(actual.exception) : "none")

                    .build();

                if (expected instanceof Exception && !Exception.class.isAssignableFrom(ReflectionUtilsP.stringToMethod(node.get("entryPoint").asText()).getReturnType())) {
                    assertNotNull(actual.exception, context, r -> "BuildRail() did not throw an exception!");
                    assertEquals(expected.getClass(), actual.exception.getClass(), context, r -> "BuildRail() did not throw an exception of the expected Type");
                    return;
                }

                Assertions2.assertEquals(expected, actual.invoked, context, r -> "BuildRail() did not return the expected value!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideBuildRail() {
        return Project_TestP.parseJsonFile("hProjekt/controller/PlayerController_buildRail.json");
    }


    @ParameterizedTest
    @MethodSource("provideCanBuildRail")
    public void testCanBuildRail(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.PlayerController.class.getDeclaredMethod("canBuildRail", hProjekt.model.Edge.class);
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }


        Throwable lastCall = null;
        for (StudentMethodCall actual: results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                    .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                    .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                    .build();

                Assertions2.assertEquals(expected, actual.call.returnValue(), context, r -> "CanBuildRail() did not return the expected value!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideCanBuildRail() {
        return Project_TestP.parseJsonFile("hProjekt/controller/PlayerController_canBuildRail.json");
    }


    @ParameterizedTest
    @MethodSource("provideCanDrive")
    public void testCanDrive(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.PlayerController.class.getDeclaredMethod("canDrive");
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }


        Throwable lastCall = null;
        for (StudentMethodCall actual: results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                    .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                    .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                    .build();

                Assertions2.assertEquals(expected, actual.call.returnValue(), context, r -> "CanDrive() did not return the expected value!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideCanDrive() {
        return Project_TestP.parseJsonFile("hProjekt/controller/PlayerController_canDrive.json");
    }


    @ParameterizedTest
    @MethodSource("provideDrive")
    public void testDrive(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.PlayerController.class.getDeclaredMethod("drive", hProjekt.model.Tile.class);
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node);

        Throwable lastCall = null;
        for (StudentMethodCall actual: results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                    .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                    .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                    .add("Exception class", actual.exception != null ? actual.exception.getClass() : "none")
                    .add("Exception message", actual.exception != null ? actual.exception.getMessage(): "none")
                    .add("Exception stacktrace", actual.exception != null ? ReflectionUtilsP.formatStackTrace(actual.exception) : "none")

                    .build();

                if (expected instanceof Exception && !Exception.class.isAssignableFrom(ReflectionUtilsP.stringToMethod(node.get("entryPoint").asText()).getReturnType())) {
                    assertNotNull(actual.exception, context, r -> "Drive() did not throw an exception!");
                    assertEquals(expected.getClass(), actual.exception.getClass(), context, r -> "Drive() did not throw an exception of the expected Type");
                    return;
                }

                Assertions2.assertEquals(expected, actual.invoked, context, r -> "Drive() did not return the expected value!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideDrive() {
        return Project_TestP.parseJsonFile("hProjekt/controller/PlayerController_drive.json");
    }


    @ParameterizedTest
    @MethodSource("provideGetBuildableRails")
    public void testGetBuildableRails(ObjectNode node) throws NoSuchMethodException {
        hProjekt.controller.PlayerController.class.getDeclaredMethod("getBuildableRails");
        Object expected = new MockConverterP().fromJsonNodeWithBackfill((ObjectNode) node.get("expected"), null);
        List<StudentMethodCall> results = MockConverterP.recreateCallAndInvoke(node);

        if (results.stream().allMatch(it -> it.exception != null)) {
            ReflectionUtilsP.getUnsafe().throwException(results.getLast().exception);
        }


        Throwable lastCall = null;
        for (StudentMethodCall actual: results) {
            if (actual.call == null) {
                lastCall = actual.exception;
                continue;
            }
            try {
                Context context = contextBuilder()
                    .add("invoked", actual.invoked != null ? actual.invoked : "unknown")
                    .add("parameters", actual.call != null ? actual.call.arguments() : "unknown")
                    .build();

                assertSetEquals(
                    ((Set<EdgeImpl>) (expected)),
                    ((Set<EdgeImpl>) (actual.call.returnValue())), (e, a) ->
                    {
                        boolean sameOwners = ReflectionUtilsP.equalsForMocks(a.railOwners(), e.railOwners());
                        boolean sameLocation = (e.position1().equals(a.getPosition1()) &&
                            e.position2().equals(a.getPosition2())) ||
                            (e.position1().equals(a.getPosition2()) && e.position2().equals(a.getPosition1()));
                        return sameOwners && sameLocation;
                    },
                    context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetBuildableRails() {
        return Project_TestP.parseJsonFile("hProjekt/controller/PlayerController_getBuildableRails.json");
    }

}
