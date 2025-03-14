package hProjekt.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import hProjekt.Project_TestP;
import hProjekt.mocking.MockConverterP;
import hProjekt.mocking.ReflectionUtilsP;
import hProjekt.mocking.StudentMethodCall;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Context;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static hProjekt.Project_TestP.assertEqualsWithMatcher;
import static hProjekt.Project_TestP.assertSetEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertEquals;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertNotNull;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;

@TestForSubmission
public class TileImplTest {

    @ParameterizedTest
    @MethodSource("provideGetConnectedNeighbours")
    public void testGetConnectedNeighbours(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.TileImpl.class.getDeclaredMethod("getConnectedNeighbours", java.util.Set.class);
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

                assertSetEquals((Set<Object>) expected, (Set<Object>) actual.call.returnValue(), context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetConnectedNeighbours() {
        return Project_TestP.parseJsonFile("hProjekt/model/TileImpl_getConnectedNeighbours.json");
    }


    @ParameterizedTest
    @MethodSource("provideGetEdge")
    public void testGetEdge(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.TileImpl.class.getDeclaredMethod("getEdge", hProjekt.model.TilePosition.EdgeDirection.class);
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

                assertEqualsWithMatcher(
                    ((EdgeImpl) (expected)), ((EdgeImpl) (actual.call.returnValue())), (e, a) ->
                    {
                        if ((e == null) && (a == null)) {
                            return true;
                        }
                        if ((e == null) || (a == null)) {
                            return false;
                        }
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

    private static Stream<Arguments> provideGetEdge() {
        return Project_TestP.parseJsonFile("hProjekt/model/TileImpl_getEdge.json");
    }


    @ParameterizedTest
    @MethodSource("provideGetNeighbour")
    public void testGetNeighbour(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.TileImpl.class.getDeclaredMethod("getNeighbour", hProjekt.model.TilePosition.EdgeDirection.class);
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

                assertEqualsWithMatcher(
                    ((TileImpl) (expected)), ((TileImpl) (actual.call.returnValue())), (e, a) ->
                    {
                        if ((e == null) && (a == null)) {
                            return true;
                        }
                        if ((e == null) || (a == null)) {
                            return false;
                        }
                        return ReflectionUtilsP.equalsForMocks(e.getPosition(), a.getPosition());
                    },
                    context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetNeighbour() {
        return Project_TestP.parseJsonFile("hProjekt/model/TileImpl_getNeighbour.json");
    }

}
