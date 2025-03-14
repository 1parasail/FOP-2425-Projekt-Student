package hProjekt.model;

import static hProjekt.Project_TestP.assertContainsAll;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.assertTrue;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;

import com.fasterxml.jackson.databind.node.ObjectNode;

import hProjekt.Project_TestP;
import hProjekt.mocking.MockConverterP;
import hProjekt.mocking.ReflectionUtilsP;
import hProjekt.mocking.StudentMethodCall;

@TestForSubmission
public class HexGridImplTest {

    @ParameterizedTest
    @MethodSource("provideGetConnectedCities")
    public void testGetConnectedCities(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.HexGridImpl.class.getDeclaredMethod("getConnectedCities");
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

                try {
                    assertTrue(
                            Class.forName("java.util.Collections$UnmodifiableMap").isAssignableFrom(
                                    actual.call.returnValue().getClass()) ||
                                    actual.call.returnValue().getClass().getName().contains(

                                            "java.util.ImmutableCollections"),
                            context,
                            r -> "getConnectedCities() did not return immutable map! Returned Object of class " +
                                    actual.call.returnValue().getClass());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                ;
                assertContainsAll(((Map<Object, Object>) (expected)),
                        ((Map<Object, Object>) (actual.call.returnValue())), context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetConnectedCities() {
        return Project_TestP.parseJsonFile("hProjekt/model/HexGridImpl_getConnectedCities.json");
    }

    @ParameterizedTest
    @MethodSource("provideGetStartingCities")
    public void testGetStartingCities(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.HexGridImpl.class.getDeclaredMethod("getStartingCities");
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

                assertContainsAll((Map<Object, Object>) expected, (Map<Object, Object>) actual.call.returnValue(),
                        context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetStartingCities() {
        return Project_TestP.parseJsonFile("hProjekt/model/HexGridImpl_getStartingCities.json");
    }

    @ParameterizedTest
    @MethodSource("provideGetUnconnectedCities")
    public void testGetUnconnectedCities(ObjectNode node) throws NoSuchMethodException {
        hProjekt.model.HexGridImpl.class.getDeclaredMethod("getUnconnectedCities");
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

                try {
                    assertTrue(
                            Class.forName("java.util.Collections$UnmodifiableMap").isAssignableFrom(
                                    actual.call.returnValue().getClass()) ||
                                    actual.call.returnValue().getClass().getName().contains(

                                            "java.util.ImmutableCollections"),
                            context,
                            r -> "getUnconnectedCities() did not return immutable map! Returned Object of class " +
                                    actual.call.returnValue().getClass());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                ;
                assertContainsAll(((Map<Object, Object>) (expected)),
                        ((Map<Object, Object>) (actual.call.returnValue())), context);
                return;
            } catch (Throwable e) {
                lastCall = e;
            }
        }
        ReflectionUtilsP.getUnsafe().throwException(lastCall);
    }

    private static Stream<Arguments> provideGetUnconnectedCities() {
        return Project_TestP.parseJsonFile("hProjekt/model/HexGridImpl_getUnconnectedCities.json");
    }

}
