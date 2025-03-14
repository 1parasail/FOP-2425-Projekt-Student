package hProjekt.model;

import static hProjekt.Project_TestP.assertSetEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Context;

import com.fasterxml.jackson.databind.node.ObjectNode;

import hProjekt.Project_TestP;
import hProjekt.mocking.MockConverterP;
import hProjekt.mocking.ReflectionUtilsP;
import hProjekt.mocking.StudentMethodCall;

@TestForSubmission
public class EdgeImplTest {

    @ParameterizedTest
    @MethodSource("provideAddRail")
    public void testAddRail(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.EdgeImpl.class.getDeclaredMethod("addRail", hProjekt.model.Player.class);
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

                Assertions2.assertEquals(expected, actual.call.returnValue(), context,
                        r -> "AddRail() did not return the expected value!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideAddRail() {
        return Project_TestP.parseJsonFile("hProjekt/model/EdgeImpl_addRail.json");
    }

    @ParameterizedTest
    @MethodSource("provideConnectsTo")
    public void testConnectsTo(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.EdgeImpl.class.getDeclaredMethod("connectsTo", hProjekt.model.Edge.class);
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

                Assertions2.assertEquals(expected, actual.call.returnValue(), context,
                        r -> "ConnectsTo() did not return the expected value!");
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideConnectsTo() {
        return Project_TestP.parseJsonFile("hProjekt/model/EdgeImpl_connectsTo.json");
    }

    @ParameterizedTest
    @MethodSource("provideGetConnectedEdges")
    public void testGetConnectedEdges(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.EdgeImpl.class.getDeclaredMethod("getConnectedEdges");
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

                assertSetEquals(
                        ((Set<EdgeImpl>) (expected)),
                        ((Set<EdgeImpl>) (actual.call.returnValue())), (e, a) -> {
                            boolean sameOwners = ReflectionUtilsP.equalsForMocks(a.railOwners(), e.railOwners());
                            boolean sameLocation = (e.position1().equals(a.getPosition1())
                                    && e.position2().equals(a.getPosition2()))
                                    || (e.position1().equals(a.getPosition2())
                                            && e.position2().equals(a.getPosition1()));
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

    private static Stream<Arguments> provideGetConnectedEdges() {
        return Project_TestP.parseJsonFile("hProjekt/model/EdgeImpl_getConnectedEdges.json");
    }
}
