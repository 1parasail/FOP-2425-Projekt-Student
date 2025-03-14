package hProjekt.mocking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Streams;
import hProjekt.DoNotMock;
import hProjekt.model.EdgeImpl;
import kotlin.Pair;
import kotlin.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentest4j.AssertionFailedError;
import org.tudalgo.algoutils.student.CrashException;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.match.BasicStringMatchers;
import org.tudalgo.algoutils.tutor.general.match.MatchingUtils;
import org.tudalgo.algoutils.tutor.general.reflections.PackageLink;
import org.tudalgo.algoutils.tutor.general.reflections.TypeLink;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static hProjekt.mocking.ReflectionUtilsP.actsLikePrimitive;
import static hProjekt.mocking.ReflectionUtilsP.filterCopiedFields;
import static hProjekt.mocking.ReflectionUtilsP.formatStackTrace;
import static hProjekt.mocking.ReflectionUtilsP.getExercisePrefix;
import static hProjekt.mocking.ReflectionUtilsP.isObjectMethod;
import static hProjekt.mocking.ReflectionUtilsP.methodToString;
import static hProjekt.mocking.ReflectionUtilsP.stringToMethod;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.fail;

public class MockConverterP extends JsonConverterP {

    private static final boolean DUMP_METHOD_CALLS = false;
    private static final boolean DUMP_MAPPED_OBJECTS = false;
    public static String SOLUTION_PACKAGE_INFIX = "solution";

    protected Map<Class<?>, Function<Object, Object>> solutionMapper = new HashMap<>() {{
        put(
            List.class,
            (list) -> ((List<?>) list).stream()
                .map(e -> getStudentObjectForSolution(e))
                .collect(Collectors.toCollection(hProjekt.mocking.ArrayList::new))
        );
        put(
            Set.class,
            (list) -> ((Set<?>) list).stream()
                .map(e -> getStudentObjectForSolution(e))
                .collect(Collectors.toCollection(ArraySet::new))
        );
        put(
            Map.class,
            (map) -> new NonHashMap<>(((Map<?, ?>) map).entrySet()
                .stream()
                .map(e -> Map.entry(getStudentObjectForSolution(e.getKey()), getStudentObjectForSolution(e.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
        );
        put(
            Map.Entry.class,
            (entry) -> Map.entry(
                getStudentObjectForSolution(((Entry<?, ?>) entry).getKey()),
                getStudentObjectForSolution(((Entry<?, ?>) entry).getValue())
            )
        );
    }};

    private static Boolean hasSolution;
    private BiMap<Integer, Object> objects = HashBiMap.create();
    private Map<Integer, ObjectNode> mapped = new HashMap<>();
    private BiMap<Object, Object> solutionMocks = HashBiMap.create();
    private List<Triple<Object, String, ObjectNode>> backfill = new ArrayList<>();
    private Method entryPoint;
    private boolean remap = false;

    public MockConverterP() {
    }

    public MockConverterP(boolean shouldApplyRemapping) {
        remap = shouldApplyRemapping;
    }

    public MockConverterP(Method entryPoint) {
        this.entryPoint = entryPoint;
    }

    @Override
    public ObjectNode toJsonNode(Object toMap) {
        int identityHashCode = System.identityHashCode(toMap);
        if (mapped.containsKey(identityHashCode)) {
            ObjectNode recursionPrevention = MAPPER.createObjectNode();
            recursionPrevention.put("id", identityHashCode);
            return recursionPrevention;
//            return mapped.get(identityHashCode);
        }
        if (objects.containsValue(toMap) && ReflectionUtilsP.actsLikePrimitive(toMap.getClass())) {
            return mapped.get(System.identityHashCode(objects.get(objects.inverse().get(toMap))));
        }

        ObjectNode recursionPrevention = MAPPER.createObjectNode();
        recursionPrevention.put("id", identityHashCode);
        mapped.put(identityHashCode, recursionPrevention);

        ObjectNode rootNode = super.toJsonNode(toMap);

        rootNode.put("id", identityHashCode);

        if (!objects.containsValue(toMap)) {
            objects.put(identityHashCode, toMap);
        }
        mapped.put(identityHashCode, rootNode);

        return rootNode;
    }

    @Override
    protected boolean filterFields(Field field) {
        return filterCopiedFields(field, entryPoint);
    }

    @Override
    public <T> T fromJsonNode(ObjectNode nodeToConvert, Answer<?> defaultAnswer) {
        if (objects.containsKey(nodeToConvert.get("id").asInt())) {
            return (T) objects.get(nodeToConvert.get("id").asInt());
        }

        T constructed = super.fromJsonNode(nodeToConvert, defaultAnswer);

        if (!objects.containsValue(constructed) || System.identityHashCode(objects.get(objects.inverse().get(constructed))) != System.identityHashCode(constructed)) {
            try {
                objects.put(nodeToConvert.get("id").asInt(), constructed);
            } catch (IllegalArgumentException e){
                System.out.println("old Objects: " + objects);
                System.out.println("new Object: " + nodeToConvert.get("id").asInt() + "=" + constructed);
                throw e;
            }

        }

        if (constructed == null) {
            return null;
        }

        return (T) constructed;
    }

    public <T> T fromJsonNodeWithBackfill(ObjectNode nodeToConvert, Answer<?> defaultAnswer) {
        T rtn;
        try {
            rtn = fromJsonNode(nodeToConvert, defaultAnswer);
        } catch (RuntimeException e) {
            e.getCause().printStackTrace();
            rtn = fromJsonNode(nodeToConvert, defaultAnswer);
        }

        backfill.forEach(set -> {
            ReflectionUtilsP.setFieldValue(set.component1(), set.component2(), fromJsonNode(set.component3(), defaultAnswer));
        });
        return rtn;
    }

    @Override
    protected <T> void setFieldValues(Answer<?> defaultAnswer, ObjectNode fieldNode, T constructed) {
        try {
            super.setFieldValues(defaultAnswer, fieldNode, constructed);
        } catch (Exception e) {
            String fieldName = fieldNode.get("name").asText();

            backfill.add(new Triple<>(constructed, fieldName, fieldNode));
        }
    }

    public void reset() {
        objects = HashBiMap.create();
        mapped = new HashMap<>();
        solutionMocks = HashBiMap.create();
    }

    public static Pair<ObjectNode, StudentMethodCall> mapCall(Object objectToCall, Method method, boolean includeObjectMethods, Object... arguments)
        throws IllegalAccessException {
        MockConverterP converter = new MockConverterP(method);

        Map<Object, Map<Method, Set<Invocation>>> calls = new HashMap<>();

        AtomicBoolean stopRecordingCalls = new AtomicBoolean(false);

        Predicate<Method> methodFilter = (calledMethod) -> {
            if (!calledMethod.getDeclaringClass().getName().startsWith(getExercisePrefix(objectToCall.getClass()))){
                return true;
            }
            if (calledMethod.getName().startsWith("set")
                && calledMethod.getParameters().length == 1
                && Arrays.stream(calledMethod.getDeclaringClass().getDeclaredFields())
                .anyMatch(f -> f.getName().equalsIgnoreCase(calledMethod.getName().replace("set", "")))) {
                return true;
            }
            return Streams.concat(
                Arrays.stream(calledMethod.getDeclaringClass().getAnnotations()),
                Arrays.stream(calledMethod.getAnnotations())
            )
                .anyMatch(ann -> {
                    if (ann.annotationType() == DoNotTouch.class) {
                        return true;
                    }
                    if (ann instanceof DoNotMock doNotMock && (doNotMock.value().length == 0 || Arrays.stream(doNotMock.value()).map(ReflectionUtilsP::stringToMethod).anyMatch(method::equals))) {
                        return true;
                    }
                    return false;
                }
            );
        };

        Answer<?> answer = invocationOnMock -> {
            Method calledMethod = invocationOnMock.getMethod();

            var returnValue = invocationOnMock.callRealMethod();
            if (methodFilter.test(calledMethod)){
                return returnValue;
            }
            if (!stopRecordingCalls.get()) {
                calls
                    .computeIfAbsent(invocationOnMock.getMock(), (m) -> new HashMap<>())
                    .computeIfAbsent(invocationOnMock.getMethod(), (m) -> new HashSet<>())
                    .add(new Invocation(invocationOnMock.getArguments(), returnValue));
            }
            return returnValue;
        };

        Object converted = deepConvertToMocks(converter.entryPoint, objectToCall, answer);
        for (int i = 0; i < arguments.length; i++) {
            Class argClass = arguments[i].getClass();
            if (ReflectionUtilsP.isLambda(argClass)) {
                Object original = arguments[i];
                arguments[i] = mock(
                    argClass.getInterfaces()[0], invocationOnMock -> {
                        Method calledMethod = invocationOnMock.getMethod();
                        var returnValue = calledMethod.invoke(original, invocationOnMock.getArguments());
                        if (methodFilter.test(calledMethod)){
                            return returnValue;
                        }
                        if (!stopRecordingCalls.get()) {
                            calls
                                .computeIfAbsent(invocationOnMock.getMock(), (m) -> new HashMap<>())
                                .computeIfAbsent(calledMethod, (m) -> new HashSet<>())
                                .add(new Invocation(invocationOnMock.getArguments(), returnValue));
                        }
                        return returnValue;
                    }
                );
            } else {
                Object existingMock = ReflectionUtilsP.findInFields(arguments[i], converted);
                if (existingMock != null) {
                    arguments[i] = existingMock;
                } else {
                    arguments[i] = deepConvertToMocks(converter.entryPoint, arguments[i], answer);
                }

            }
        }

        converter.toJsonNode(converted);

        Object expected = null;
        Throwable exception = null;
        try {
            method.setAccessible(true);
            if (!method.getReturnType().equals(void.class)) {
                expected = method.invoke(converted, arguments);
            } else {
                method.invoke(converted, arguments);
                expected = converted;
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Can not invoke %s on Class %s with parameters of type %s".formatted(
                    method.getName(),
                    objectToCall.getClass(),
                    Arrays.stream(arguments).map(java.lang.Object::getClass).toList()
                ), e
            );
        } catch (InvocationTargetException e) {
            exception = e.getCause();
            System.err.println("Encountered Exception when executing Code. This may be wanted or unwanted. Be sure it is wanted!");
            exception.printStackTrace();
        } finally {
            stopRecordingCalls.set(true);
        }

        for (Object argument : arguments) {
            converter.toJsonNode(argument);
        }
        for (Object called : calls.keySet()) {
            converter.toJsonNode(called);
        }

        ObjectNode rootNode = MAPPER.createObjectNode();

        converter.setupEntryPoint(arguments, rootNode, converted, method);

        if (DUMP_METHOD_CALLS){
            System.out.println("Number of calls: " + calls.size());
            calls.entrySet().forEach( invoked -> {
                    System.out.println("Invoked: " + invoked.getKey());
                    System.out.println("    Invokations: ");
                    invoked.getValue().entrySet().forEach(methods -> {
                            System.out.println("        Method: " + methods.getKey());
                            System.out.println("        Parameters: " + methods.getValue().stream().flatMap(it -> Arrays.stream(it.arguments())).toList());
                        }

                    );
                }
            );
        }

        converter.setupMethodCalls(method, includeObjectMethods, calls, rootNode);

        if (DUMP_MAPPED_OBJECTS) {
            List<Map.Entry<Class<?>, Integer>> elements = (List<Map.Entry<Class<?>, Integer>>) (List) new ArrayList<>(converter.objects.values().stream().filter(Objects::nonNull).collect(Collectors.groupingBy(Object::getClass)).entrySet().stream().map(it -> Map.entry(it.getKey(), it.getValue().size())).toList());
            elements.sort(Map.Entry.comparingByValue());
            elements = elements.reversed();
            System.out.println(elements);
        }

        converter.setUpObjects(rootNode);

        rootNode.set("expected", new MockConverterP(method).toJsonNode(expected != null ? expected : exception));

        return new Pair<>(rootNode, new StudentMethodCall(converted, new Invocation(arguments, expected), exception));
    }

    private void setupEntryPoint(Object[] arguments, ObjectNode rootNode, Object converted, Method invokedMethod) {
        rootNode.put("invoked", getID(converted));
        rootNode.put("entryPoint", methodToString(invokedMethod));
        ArrayNode argumentsJson = MAPPER.createArrayNode();
        for (Object argument : arguments) {
            argumentsJson.add(getID(argument));
        }
        rootNode.set("arguments", argumentsJson);
    }

    private void setUpObjects(ObjectNode rootNode) {
        ArrayNode jsonObjects = MAPPER.createArrayNode();
        mapped.values().stream().sorted((a, b) -> -(a.toString().length() - b.toString().length())).forEach(object -> {
            if (containsNode(jsonObjects, object)) {
                return;
            }
            jsonObjects.add(object);
        });

        rootNode.set("objects", jsonObjects);
    }

    private void setupMethodCalls(Method method, boolean includeObjectMethods, Map<Object, Map<Method, Set<Invocation>>> calls,
                                  ObjectNode rootNode) {
        ArrayNode jsonCalls = MAPPER.createArrayNode();
        for (Map.Entry<Object, Map<Method, Set<Invocation>>> called : calls.entrySet()) {
            Object invokedObject = called.getKey();
            ObjectNode objectNode = MAPPER.createObjectNode();

            objectNode.put("id", getID(invokedObject));
            ArrayNode methodCalls = MAPPER.createArrayNode();
            for (Map.Entry<Method, Set<Invocation>> methods : called.getValue().entrySet()) {
                ObjectNode methodNode = MAPPER.createObjectNode();

                if (!includeObjectMethods && isObjectMethod(methods.getKey()) ||
                    (method.getName().equals(methods.getKey().getName()) &&
                        Arrays.equals(method.getParameters(), methods.getKey().getParameters()
                        )
                    )
                ) {
                    continue;
                }

                methodNode.put("methodName", methods.getKey().getName());

                ArrayNode invocations = MAPPER.createArrayNode();

                for (Invocation invocation : methods.getValue()) {
                    ObjectNode invocationNode = MAPPER.createObjectNode();

                    if (getID(invocation.returnValue()) == -1) {
                        toJsonNode(invocation.returnValue());
                    }
                    invocationNode.put("return", getID(invocation.returnValue()));

                    ArrayNode parameters = MAPPER.createArrayNode();
                    for (Object parameter : invocation.arguments()) {
                        if (getID(parameter) == -1) {
                            toJsonNode(parameter);
                        }
                        parameters.add(getID(parameter));
                    }
                    invocationNode.set("parameter", parameters);

                    invocations.add(invocationNode);
                }

                methodNode.set("invocations", invocations);

                methodCalls.add(methodNode);
            }
            objectNode.set("methodCalls", methodCalls);

            if (!objectNode.get("methodCalls").isEmpty()) {
                jsonCalls.add(objectNode);
            }
        }
        rootNode.set("calls", jsonCalls);
    }

    public static List<StudentMethodCall> recreateCallAndInvoke(ObjectNode node) {
        return recreateCallAndInvoke(node, null);
    }

    public static List<StudentMethodCall> recreateCallAndInvoke(ObjectNode node, Runnable beforeEach) {

        List<StudentMethodCall> results = new ArrayList<>();

        Class<?> expectedType = getTypeFromNode((ObjectNode) node.get("expected"));

        if (beforeEach != null){
            beforeEach.run();
        }
        try {
            results.add(recreateCallAndInvokeUnMocked(node));
        } catch (Throwable e) {
            results.add(new StudentMethodCall(null, null, e));
        }
        System.out.println("started mocking");
        if (beforeEach != null){
            beforeEach.run();
        }
        try {
            results.add(recreateCallAndInvokeWithMock(node, false));
        } catch (Throwable e) {
            results.add(new StudentMethodCall(null, null, e));
        }
        StudentMethodCall solResult;
        if (beforeEach != null){
            beforeEach.run();
        }
        try {
            solResult = recreateCallAndInvokeWithMock(node, true);
        } catch (Throwable e) {
            solResult = new StudentMethodCall(null, null, e);
        }
        if (hasSolution != null && hasSolution){
            results.add(solResult);
        }

        results.forEach(it -> {
            if (it.exception != null && it.exception instanceof StudentImplementationException sie) {
                if (expectedType != null && !Throwable.class.isAssignableFrom(expectedType)) {
                    try {
                        fail(sie.getContext(), r -> sie.getMessage());
                    } catch (Throwable e) {
                        it.setException(e);
                    }
                } else {
                    it.exception = it.exception.getCause();
                }
            }
        });

        return results;
    }

    private static StudentMethodCall recreateCallAndInvokeUnMocked(ObjectNode node) {
        MockConverterP converter = new MockConverterP(false);

        Method entryPoint = stringToMethod(node.get("entryPoint").asText());

        converter.createObjects(node, CALLS_REAL_METHODS);

        Object invoked = converter.objects.get(node.get("invoked").asInt());

        Object[] arguments = Streams.stream(node.get("arguments")).map(id -> converter.objects.get(id.asInt())).toArray();

        Object returnValue = null;
        Throwable exception = null;
        try {
            returnValue = ReflectionUtilsP.callMethod(invoked, entryPoint, arguments);
        } catch (Throwable t) {
            exception = t;
        }

        return new StudentMethodCall(invoked, new Invocation(arguments, returnValue), exception);
    }

    private static StudentMethodCall recreateCallAndInvokeWithMock(ObjectNode node, boolean useFullSolution) {
        MockConverterP converter = new MockConverterP(useFullSolution);

        Method entryPoint = stringToMethod(node.get("entryPoint").asText());

        Object invoked = recreateObjectsAndCalls(node, converter, entryPoint);

        Object[] arguments = Streams.stream(node.get("arguments")).map(id -> converter.objects.get(id.asInt())).toArray();

        Object returnValue = null;
        Throwable exception = null;
        try {
            returnValue = ReflectionUtilsP.callMethod(invoked, entryPoint, arguments);
        } catch (Throwable t) {
            exception = t;
        }

        return new StudentMethodCall(invoked, new Invocation(arguments, returnValue), exception);
    }

    public static <T> T recreateObjectsAndCalls(ObjectNode node, Method entryPoint) {
        return recreateObjectsAndCalls(node, new MockConverterP(true), entryPoint);
    }

    public static <T> T recreateObjectsAndCalls(ObjectNode node, MockConverterP converter, Method entryPoint) {
        Map<Integer, Map<Method, List<Invocation>>> calls = new HashMap<>();

        Answer<?> defaultAnswer = converter.createDefaultAnswer(calls, entryPoint);

        converter.createObjects(node, defaultAnswer);

        if (converter.remap) {
            Pattern valuePattern = Pattern.compile("\"type\":\"(?<className>[a-zA-Z0-9.-]*)\"");

            var matcher = valuePattern.matcher(node.toString());

            StringBuffer remapped = new StringBuffer();
            while (matcher.find()) {
                String className = matcher.group("className");
                if (className.contains(SOLUTION_PACKAGE_INFIX) || className.contains("null")) {
                    continue;
                }
                try {
                    Class<?> studentClass = Class.forName(className);
                    Class<?> solClass = getSolution(studentClass);
                    if (solClass != null) {
                        matcher.appendReplacement(remapped, "\"type\":\"" + solClass.getName() + "\"");
                    }
                } catch (ClassNotFoundException e) {
                    // can be ignored as this should never happen
                    throw new RuntimeException(e);
                }
            }
            matcher.appendTail(remapped);

            ObjectNode mockNode;
            try {
                mockNode = (ObjectNode) MAPPER.readTree(remapped.toString());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            MockConverterP solConverter = new MockConverterP(false);
            solConverter.createObjects(mockNode, CALLS_REAL_METHODS);

            solConverter.objects.forEach((id, solMock) -> {
                if (solMock.getClass().getName().contains("org.mockito.codegen")) {
                    return;
                }
                converter.solutionMocks.put(converter.objects.get(id), solMock);
            });
        }

        converter.createCalls(node, calls);

        return (T) converter.objects.get(node.get("invoked").asInt());
    }

    private void createCalls(ObjectNode node, Map<Integer, Map<Method, List<Invocation>>> calls) {
        ArrayNode callNode = (ArrayNode) node.get("calls");
        for (JsonNode call : callNode) {
            int objectID = call.get("id").asInt();
            Object object = objects.get(objectID);
            var objectMap = calls.computeIfAbsent(objectID, (id) -> new HashMap<>());

            ArrayNode methodNode = (ArrayNode) call.get("methodCalls");
            for (JsonNode method : methodNode) {
                Method methodObject = null;

                for (JsonNode invocation : method.get("invocations")) {
                    ArrayNode parameterNode = (ArrayNode) invocation.get("parameter");
                    List<Object> parameters = new ArrayList<>();

                    for (JsonNode parameter : parameterNode) {
                        parameters.add(objects.get(parameter.asInt()));
                    }

                    try {
                        if (methodObject == null) {
                            methodObject = ReflectionUtilsP.getMethodForParameters(
                                method.get("methodName").asText(),
                                object.getClass(),
                                parameters,
                                false
                            );
                        }

                        var methodMap = objectMap.computeIfAbsent(methodObject, (m) -> new ArrayList<>());
                        methodMap.add(new Invocation(
                            parameters.toArray(),
                            objects.get(invocation.get("return").asInt())
                        ));

                    } catch (NoSuchMethodException ignored) {
                    }
                }
            }
        }
    }

    private void createObjects(ObjectNode node, Answer<?> defaultAnswer) {
        ArrayNode objects = (ArrayNode) node.get("objects");
        List<JsonNode> retry = new ArrayList<>();

        for (JsonNode object : objects) {
            try {
                fromJsonNode((ObjectNode) object, defaultAnswer);
            } catch (RuntimeException e) {
                retry.add(object);
            }
        }

        for (JsonNode object : retry) {
            fromJsonNode((ObjectNode) object, defaultAnswer);
        }

        backfill.forEach(set -> {
            ReflectionUtilsP.setFieldValue(set.component1(), set.component2(), fromJsonNode(set.component3(), defaultAnswer));
        });
    }

    private @NotNull Answer<Object> createDefaultAnswer(Map<Integer, Map<Method, List<Invocation>>> calls, Method entryPoint) {
        return (mockInvocation) -> {

            if (mockInvocation.getMethod().equals(entryPoint)) {
                return callRealMethod(mockInvocation);
            }

            //replace call with call to solution
            if (solutionMocks.containsKey(mockInvocation.getMock())) {
                return replaceCallWithSolution(mockInvocation);
            }

            int objectID = getID(mockInvocation.getMock());
            Map<Method, List<Invocation>> objectCalls = calls.get(objectID);

            if (objectCalls == null || !objectCalls.containsKey(mockInvocation.getMethod())) {
                return callRealMethod(mockInvocation);
            }

            List<Invocation> invocations = objectCalls.get(mockInvocation.getMethod());
            Optional<Invocation> invocation = invocations.stream()
                .filter((inv) -> Arrays.deepEquals(inv.arguments(), mockInvocation.getArguments()))
                .findFirst();

            if (invocation.isPresent()) {
                return invocation.get().returnValue();
            }

            return callRealMethod(mockInvocation);
        };
    }

    private Object callRealMethod(InvocationOnMock mockInvocation) {
        try {
            return mockInvocation.callRealMethod();
        } catch (CrashException | AssertionFailedError | StudentImplementationException e) {
            throw e;
        } catch (MockitoException e) {
            System.err.println("Tried to call \"" + mockInvocation.getMethod() + "\" on class " + mockInvocation.getMock()
                .getClass());
            throw e;
        } catch (Throwable e) {
            String stacktrace = formatStackTrace(e);

            Context context = contextBuilder()
                .add("Object", mockInvocation.getMock())
                .add("Parameters", Arrays.stream(mockInvocation.getArguments()).map(it -> it != null ? it.toString() : "null").toList())
                .add("Exception Class", e.getClass())
                .add("Message", e.getMessage())
                .add("Stacktrace", stacktrace)
                .build();

            throw new StudentImplementationException(e, context, "Method " + mockInvocation.getMethod().getName() + "() threw an exception!");
        }
    }

    private Object replaceCallWithSolution(InvocationOnMock mockInvocation)
        throws IllegalAccessException {
        Object solMock = solutionMocks.get(mockInvocation.getMock());

        //map all parameters to solution mocks if available
        Object[] params = Arrays.stream(mockInvocation.getArguments())
            .map(obj -> {
                if (solutionMocks.containsKey(obj)) {
                    return solutionMocks.get(obj);
                }
                return obj;
            }).toArray();

        Object returnedObject;
        try {
            returnedObject =
                ReflectionUtilsP.getMethodForParameters(mockInvocation.getMethod().getName(), solMock.getClass(), List.of(params))
                    .invoke(solMock, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find Method %s in Class %s".formatted(
                mockInvocation.getMethod().getName(),
                solMock.getClass()
            ));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CrashException crash) {
                throw crash;
            }
            String stacktrace = formatStackTrace(cause);

            Context context = contextBuilder()
                .add("Class", mockInvocation.getMock().getClass())
                .add("Method", mockInvocation.getMethod().getName())
                .add("Object", mockInvocation.getMock())
                .add(
                    "Parameters",
                    Arrays.stream(mockInvocation.getArguments()).map(java.lang.Object::toString).collect(Collectors.joining(", "))
                )
                .add("Exception Class", cause.getClass())
                .add("Exception message", cause.getMessage())
                .add("Stacktrace", stacktrace)
                .build();
            fail(context, r -> "Could not correctly invoke Solution as it threw an exception!");
            throw new RuntimeException();
        }

        return getStudentObjectForSolution(returnedObject);
    }

    private @Nullable Object getStudentObjectForSolution(Object returnedObject) {

        if (returnedObject == null) {
            return null;
        } else if (solutionMocks.inverse().containsKey(returnedObject)) {
            return solutionMocks.inverse().get(returnedObject);
        } else if (actsLikePrimitive(returnedObject.getClass())) {
            return returnedObject;
        } else if (typeMapperJSON.keySet()
            .stream()
            .anyMatch(mappedClazz -> mappedClazz.isAssignableFrom(returnedObject.getClass()))) {

            Function<Object, Object> solMapper = solutionMapper.entrySet()
                .stream()
                .filter(mappedClazz -> mappedClazz.getKey().isAssignableFrom(returnedObject.getClass()))
                .findFirst()
                .get()
                .getValue();

            return solMapper.apply(returnedObject);
        } else if (getStudentClass(returnedObject.getClass()) == null) {
            return returnedObject;
        } else {
            for (Map.Entry<Object, Object> mockEntry : solutionMocks.entrySet()) {
                if (ReflectionUtilsP.equalsForMocks(mockEntry.getValue(), returnedObject)) {
                    return mockEntry.getKey();
                }
            }

            Object studentObject = mock(getStudentClass(returnedObject.getClass()));

            ReflectionUtilsP.copyFields(returnedObject, studentObject);
            solutionMocks.put(studentObject, returnedObject);
            System.out.println("returned new mock");
            return studentObject;
        }
    }

    private Integer getID(Object object) {
        if (!objects.containsValue(object)) {
            return -1;
        }
        return objects.inverse().get(object);
    }

    public static <T> T deepConvertToMocks(T toConvert, Answer<?> defaultAnswer) {
        var json = new MockConverterP().toJsonNode(toConvert);
        return new MockConverterP().fromJsonNodeWithBackfill(json, defaultAnswer);
    }

    public static <T> T deepConvertToMocks(Method entryPoint, T toConvert, Answer<?> defaultAnswer) {
        return ReflectionUtilsP.deepCloneAsMock(toConvert, defaultAnswer, entryPoint);
        //        var json = new MockConverterP(entryPoint).toJsonNode(toConvert);
//        try {
//            System.out.println("Starting Converting deep");
//            return new MockConverterP(entryPoint).fromJsonNodeWithBackfill(json, defaultAnswer);
//        } finally {
//            System.out.println("Done converting");
//        }
    }

    private static BiMap<Class<?>, Class<?>> solutions = HashBiMap.create();

    @SuppressWarnings("removal")
    public static Class<?> getSolution(Class<?> studentClass) {
        if (solutions.containsKey(studentClass)) {
            return solutions.get(studentClass);
        }

        String classPackageName = studentClass.getPackageName();
        //test if class is even part of exercise

        if (!classPackageName.matches("h[a-zA-Z0-9]{2,}(\\.|$).*")) {
            return null;
        }

        if (hasSolution != null && !hasSolution) {
            return null;
        }

        Package closestPackage = ReflectionUtilsP.getAllPackagesInExercise(studentClass).stream()
            .min((a, b) -> {
                    double simA = MatchingUtils.similarity(a.getName(), classPackageName);
                    double simB = MatchingUtils.similarity(b.getName(), classPackageName);

                    if (simA > simB) {
                        return -1;
                    } else if (simB > simA) {
                        return 1;
                    }
                    return 0;
                }
            ).orElse(null);

        if (closestPackage == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("(?<exercise>[a-zA-Z0-9]{3,})(\\.(?<package>.*))?");
        Matcher matcher = pattern.matcher(closestPackage.getName());

        //needed for match to work. return may be ignored
        matcher.matches();

        String exercise = matcher.group("exercise") + ".";

        String pack = matcher.group("package") != null ? "." + matcher.group("package") : "";

        String solutionPackage = exercise + SOLUTION_PACKAGE_INFIX + pack;

        PackageLink packageLink = ReflectionUtilsP.getPackageLink(solutionPackage);

        TypeLink link = packageLink.getType(BasicStringMatchers.similar(studentClass.getSimpleName(), 0.75));
        if (link == null) {
            hasSolution = false;
            return null;
        }

        solutions.put(studentClass, link.reflection());

        return link.reflection();
    }

    public static Class<?> getStudentClass(Class<?> solutionClass) {
        return solutions.inverse().get(solutionClass);
    }

    public static boolean containsNode(JsonNode object, JsonNode toFind) {
        for (JsonNode inner : object) {
            if ((inner.equals(toFind) || inner.get("id") != null && inner.get("id").asInt() == toFind.get("id").asInt()) && inner.get("type") != null) {
                return true;
            }
            if (containsNode(inner, toFind)) {
                return true;
            }
        }
        return false;
    }
}
