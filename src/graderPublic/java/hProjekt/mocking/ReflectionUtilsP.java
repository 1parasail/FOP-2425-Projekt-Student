package hProjekt.mocking;

import com.google.common.primitives.Primitives;
import hProjekt.MockExclude;
import hProjekt.MockInclude;
import kotlin.Pair;
import kotlin.ThrowsKt;
import org.eclipse.jdt.core.dom.MethodReference;
import org.jetbrains.annotations.NotNull;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.stubbing.Answer;
import org.opentest4j.AssertionFailedError;
import org.sourcegrade.jagr.api.testing.extension.TestCycleResolver;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.reflections.BasicPackageLink;
import org.tudalgo.algoutils.tutor.general.reflections.PackageLink;
import org.tudalgo.algoutils.tutor.general.reflections.TypeLink;
import sun.misc.Unsafe;

import java.beans.Transient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.fail;

public class ReflectionUtilsP {

    private static Map<String, Class<?>> primitiveStrings = Map.of(
        "byte", byte.class,
        "short", short.class,
        "int", int.class,
        "long", long.class,
        "float", float.class,
        "double", double.class,
        "char", char.class,
        "boolean", boolean.class
    );

    private static Set<Package> packages;

    public static void setFieldValue(Object instance, String fieldName, Object value) {
        try {
            Class<?> objectClass = instance.getClass();
            Field declaredField = null;
            for (Class<?> superClass: ReflectionUtilsP.getSuperClassesIncludingSelf(objectClass)){
                try {
                    declaredField = superClass.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {}
            }
            if (declaredField == null) {
                if (fieldName.contains("$")){
                    return;
                }
                throw new IllegalStateException("Could not find Field " + fieldName + " in Class " + objectClass);
            }

            //best case field is non Final
            if (!Modifier.isFinal(declaredField.getModifiers())) {
                try {
                    declaredField.setAccessible(true);
                    declaredField.set(instance, value);
                    return;
                } catch (Exception ignored) {
                }
            }

            //field has setter
            Optional<Method> setter = Arrays
                .stream(objectClass.getDeclaredMethods())
                .filter(
                    m -> m.getName().equalsIgnoreCase("set" + fieldName)
                ).findFirst();
            if (setter.isPresent()) {
                try {
                    setter.get().invoke(instance, value);
                    return;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Tried to access " + setter.get(), e);
                } catch (IllegalArgumentException e) {}

            }

            //rely on Unsafe to set value
            Unsafe unsafe = getUnsafe();

            Field theInternalUnsafeField = Unsafe.class.getDeclaredField("theInternalUnsafe");
            theInternalUnsafeField.setAccessible(true);
            Object theInternalUnsafe = theInternalUnsafeField.get(null);

            Method offset = Class.forName("jdk.internal.misc.Unsafe").getMethod("objectFieldOffset", Field.class);
            unsafe.putBoolean(offset, 12, true);

            if (value == null) {
                unsafe.putObject(instance, (long) offset.invoke(theInternalUnsafe, declaredField), value);
                return;
            }

//            if (!declaredField.getType().equals(value.getClass()) && (!Primitives.isWrapperType(value.getClass()) || !Primitives.unwrap(value.getClass()).equals(declaredField.getType()))) {
//                    throw new RuntimeException("Could not assign " + value + " of class " + value.getClass() + " to field " + declaredField + " of class " + declaredField.getDeclaringClass());
//            }

            switch (value) {
                case Boolean val -> unsafe.putBoolean(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Character val -> unsafe.putChar(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Short val -> unsafe.putShort(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Integer val -> unsafe.putInt(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Long val -> unsafe.putLong(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Double val -> unsafe.putDouble(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Float val -> unsafe.putFloat(instance, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                default -> unsafe.putObject(instance, (long) offset.invoke(theInternalUnsafe, declaredField), value);
            }
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException ignored) {
        }
    }

    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
        try {
            Field declaredField = null;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {}
            if (declaredField == null) {
                if (fieldName.contains("$")){
                    return;
                }
                throw new IllegalStateException("Could not find Field " + fieldName + " in Class " + clazz);
            }

            //best case field is non Final
            if (!Modifier.isFinal(declaredField.getModifiers())) {
                try {
                    declaredField.setAccessible(true);
                    declaredField.set(null, value);
                    return;
                } catch (Exception ignored) {
                }
            }

            //field has setter
            Optional<Method> setter = Arrays
                .stream(clazz.getDeclaredMethods())
                .filter(
                    m -> m.getName().equalsIgnoreCase("set" + fieldName)
                ).findFirst();
            if (setter.isPresent()) {
                try {
                    setter.get().invoke(null, value);
                    return;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Tried to access " + setter.get(), e);
                } catch (IllegalArgumentException e) {}

            }

            //rely on Unsafe to set value
            Unsafe unsafe = getUnsafe();

            Field theInternalUnsafeField = Unsafe.class.getDeclaredField("theInternalUnsafe");
            theInternalUnsafeField.setAccessible(true);
            Object theInternalUnsafe = theInternalUnsafeField.get(null);

            Method offset = Class.forName("jdk.internal.misc.Unsafe").getMethod("staticFieldOffset", Field.class);
            unsafe.putBoolean(offset, 12, true);
            final Object staticFieldBase = unsafe.staticFieldBase(declaredField);

            if (value == null) {
                unsafe.putObject(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), value);
                return;
            }

//            if (!declaredField.getType().equals(value.getClass()) && (!Primitives.isWrapperType(value.getClass()) || !Primitives.unwrap(value.getClass()).equals(declaredField.getType()))) {
//                    throw new RuntimeException("Could not assign " + value + " of class " + value.getClass() + " to field " + declaredField + " of class " + declaredField.getDeclaringClass());
//            }

            switch (value) {
                case Boolean val -> unsafe.putBoolean(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Character val -> unsafe.putChar(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Short val -> unsafe.putShort(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Integer val -> unsafe.putInt(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Long val -> unsafe.putLong(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Double val -> unsafe.putDouble(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                case Float val -> unsafe.putFloat(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), val);
                default -> unsafe.putObject(staticFieldBase, (long) offset.invoke(theInternalUnsafe, declaredField), value);
            }
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException ignored) {
        }
    }

    public static boolean isSyntheticMock(Class<?> clazz) {
        return clazz.getName().contains("org.mockito.codegen.");
    }

    public static <T, P extends T> P spyLambda(Class<T> lambdaType, P lambda, Answer<?> defaultAnswer) {
        // TODO does not work as calls cant be passed back/forth based on default answer from MockConverter
//        Answer<?> answer = mockInvocation -> {
//            try {
//                return defaultAnswer.answer(mockInvocation);
//            } catch (MockitoException e) {
//                return delegatesTo(lambda).answer(mockInvocation);
//            }
//        };
        return (P) mock(lambdaType, delegatesTo(lambda));
    }

    public static <T, P extends T> P spyLambda(Class<T> lambdaType, P lambda) {
        return (P) mock(lambdaType, delegatesTo(lambda));
    }

    public static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getFieldValue(Object instance, String fieldName) {
        Field f = null;
        Class<?> fieldType = null;
        try {
            for (Class<?> clazz: getSuperClassesIncludingSelf(instance.getClass())) {
                try {
                    f = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {}
            }
            if (f == null) {
                throw new NoSuchFieldException(fieldName);
            }

            try {
                f.setAccessible(true);
                return (T) f.get(instance);
            } catch (Exception ignored) {
            }

            fieldType = f.getType();
            if (Primitives.isWrapperType(fieldType)) {
                fieldType = Primitives.unwrap(fieldType);
            }

            Unsafe unsafe = getUnsafe();

            Field theInternalUnsafeField = Unsafe.class.getDeclaredField("theInternalUnsafe");
            theInternalUnsafeField.setAccessible(true);
            Object theInternalUnsafe = theInternalUnsafeField.get(null);

            Method offset = Class.forName("jdk.internal.misc.Unsafe").getMethod("objectFieldOffset", Field.class);
            unsafe.putBoolean(offset, 12, true);

            Object fieldValue;
            if (boolean.class == fieldType) {
                fieldValue = unsafe.getBoolean(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (byte.class == fieldType) {
                fieldValue = unsafe.getByte(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (short.class == fieldType) {
                fieldValue = unsafe.getShort(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (int.class == fieldType) {
                fieldValue = unsafe.getInt(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (long.class == fieldType) {
                fieldValue = unsafe.getLong(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (float.class == fieldType) {
                fieldValue = unsafe.getFloat(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (double.class == fieldType) {
                fieldValue = unsafe.getDouble(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else if (char.class == fieldType) {
                fieldValue = unsafe.getChar(instance, (long) offset.invoke(theInternalUnsafe, f));
            } else {
                fieldValue = unsafe.getObject(instance, (long) offset.invoke(theInternalUnsafe, f));
            }
            return (T) fieldValue;
        } catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(
                "Could not get value for Field %s(%s) in %s. Please do not access this field.".formatted(
                    fieldName,
                    fieldType,
                    instance.getClass()
                ), e
            );
        }
    }

    public static void copyFields(Object source, Object dest) {
        for (Field f : source.getClass().getDeclaredFields()) {
            setFieldValue(dest, f.getName(), getFieldValue(source, f.getName()));
        }
    }

    public static <T> T deepCloneAsMock(T toClone, Answer<?> defaultAnswer, Method entryPoint) {
        return deepCloneAsMock(toClone, defaultAnswer, entryPoint, new HashMap<>());
    }

    private static <T> T deepCloneAsMock(T toClone, Answer<?> defaultAnswer, Method entryPoint, Map<Object, Object> alreadyProcessed) {
        if (alreadyProcessed.containsKey(toClone)) {
            return (T) alreadyProcessed.get(toClone);
        }

        if (toClone == null){
            return null;
        }
        if (toClone instanceof List<?> list) {
            List<Object> newList = new ArrayList<>();
            alreadyProcessed.put(toClone, newList);
            newList.addAll(list.stream().map(item -> deepCloneAsMock(item, defaultAnswer, entryPoint, alreadyProcessed)).toList());
            return (T) newList;
        } else if (toClone instanceof Map<?,?> map) {
            Map<Object, Object> newMap = new NonHashMap<>();
            alreadyProcessed.put(toClone, newMap);
            newMap.putAll(map.entrySet().stream()
                .map(entry -> Map.entry(
                    deepCloneAsMock(entry.getKey(), defaultAnswer, entryPoint, alreadyProcessed),
                    deepCloneAsMock(entry.getValue(), defaultAnswer, entryPoint, alreadyProcessed))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            return (T) newMap;
        } else if (toClone instanceof Set<?> set) {
            Set<Object> newSet = new ArraySet<>();
            alreadyProcessed.put(toClone, newSet);
            newSet.addAll(set.stream().map(item -> deepCloneAsMock(item, defaultAnswer, entryPoint, alreadyProcessed)).toList());
            return (T) newSet;
        } else if (toClone.getClass().isArray()) {
            int length = Array.getLength(toClone);
            Object newArray = Array.newInstance(toClone.getClass().getComponentType(), length);
            alreadyProcessed.put(toClone, newArray);

            for (int i = 0; i< length; i++){
                Array.set(newArray, i, deepCloneAsMock(Array.get(toClone, i), defaultAnswer, entryPoint, alreadyProcessed));
            }
            return (T) newArray;
        } else if (actsLikePrimitive(toClone.getClass())){
            return toClone;
        }
        T clone;
        if (isLambda(toClone.getClass())){
            clone = spyLambda((Class<? super T>) getLambdaBaseClass(toClone.getClass()), toClone, defaultAnswer);
        } else {
            clone = (T) mock(toClone.getClass(), defaultAnswer);
        }


        alreadyProcessed.put(toClone, clone);

        for (Field f: getSuperClassesIncludingSelf(toClone.getClass()).stream().flatMap(it -> Arrays.stream(it.getDeclaredFields())).toList()) {
            if (filterCopiedFields(f, entryPoint)){
                continue;
            }
            Object oldValue = getFieldValue(toClone, f.getName());
            Object newValue = alreadyProcessed.get(oldValue);

            if (newValue == null){
                newValue = deepCloneAsMock(oldValue, defaultAnswer, entryPoint, alreadyProcessed);
            }

            setFieldValue(clone, f.getName(), newValue);
        }
        return clone;
    }

    public static boolean filterFieldsNonStudentDefined(Field field) {
        if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
            return true;
        }
        if (field.isSynthetic()){
            return true;
        }
        if (field.getType().equals(WeakReference.class)){
            return true;
        }
        return false;
    }

    public static boolean filterCopiedFields(Field field, Method entryPoint) {
        if (filterFieldsNonStudentDefined(field)){
            return true;
        }

        if (field.getDeclaringClass().equals(Throwable.class) && field.getName().equals("stackTrace")){
            return true;
        }

        List<MockInclude> annotations = Arrays.stream(field.getAnnotations()).filter(MockInclude.class::isInstance).map(MockInclude.class::cast).toList();

        List<Method> methods = annotations.stream().flatMap(ann -> Arrays.stream(ann.value())).map(ReflectionUtilsP::stringToMethod).toList();

        if (!annotations.isEmpty() && !methods.contains(entryPoint)){
            return true;
        }
        if (Arrays.stream(field.getAnnotations()).anyMatch(MockExclude.class::isInstance) ){
            return true;
        }
        return false;
    }

    public static boolean equalsForMocks(Object a, Object b) {
        return equalsForMocks(a, b, new ArrayList<>());
    }

    private static boolean equalsForMocks(Object a, Object b, List<Pair<Object, Object>> visited) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.equals(b) || b.equals(a)) {
            return true;
        }
        if (!a.getClass().equals(b.getClass())) {
            return false;
        }
        if (actsLikePrimitive(a.getClass())) {
            return a.equals(b);
        } else if (actsLikePrimitive(b.getClass())) {
            return b.equals(a);
        }

        if (visited.contains(new Pair<>(a, b))){
            return true;
        }
        visited.add(new Pair<>(a, b));

        for (Field f : getAllFields(a.getClass(), false).stream()
            .filter(f -> !filterFieldsNonStudentDefined(f))
            .toList()) {

            if (!equalsForMocks(getFieldValue(a, f.getName()), getFieldValue(b, f.getName()), visited)) {
                return false;
            }
        }
        return true;
    }

    public static Object findInFields(Object toFind, Object toFindIn) {
        return findInFields(toFind, toFindIn, new ArrayList<>());
    }

    private static Object findInFields(Object toFind, Object toFindIn, List<Object> searched) {
        if (searched.contains(toFindIn)) {
            return null;
        }
        searched.add(toFindIn);
        if (toFindIn == null) {
            return null;
        }
        if (actsLikePrimitive(toFindIn.getClass())) {
            return null;
        }
        if (equalsForMocks(toFind, toFindIn)) {
            return toFindIn;
        }
        if (toFindIn instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                if (element == null) {
                    continue;
                }
                Object found = findInFields(toFind, element, searched);
                if (found != null) {
                    return found;
                }
            }
        }
        if (toFindIn.getClass().isArray()) {
            int length = Array.getLength(toFindIn);
            for (int count = 0; count < length; count++) {
                Object element = Array.get(toFindIn, count);
                if (element == null) {
                    continue;
                }
                Object found = findInFields(toFind, element, searched);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        for (Field f : ReflectionUtilsP.getSuperClassesIncludingSelf(toFindIn.getClass()).stream().flatMap(it -> Arrays.stream(it.getDeclaredFields())).toList()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            Object found = findInFields(toFind, getFieldValue(toFindIn, f.getName()), searched);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public static Class<?> getClassFromPrimitiveString(String string) {
        return primitiveStrings.get(string);
    }

    public static boolean actsLikePrimitive(Class<?> type) {
        return type.isPrimitive() ||
            Enum.class.isAssignableFrom(type) ||
            Primitives.isWrapperType(type) ||
            type == String.class;
    }

    public static List<Class<?>> getSuperClassesIncludingSelf(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<>();
        Deque<Class<?>> classDeque = new ArrayDeque<>();

        classDeque.add(clazz);

        while ((clazz = classDeque.peekFirst()) != null) {
            classDeque.pop();

            classes.add(clazz);
            if (clazz.getSuperclass() != null) {
                classDeque.add(clazz.getSuperclass());
            }
            if (clazz.getInterfaces().length > 0) {
                classDeque.addAll(List.of(clazz.getInterfaces()));
            }

        }
        return classes;
    }

    public static List<Method> getAllMethods(Class<?> clazz, boolean includeMockitoSynthetic) {
        return getSuperClassesIncludingSelf(clazz).stream()
            .filter(c -> includeMockitoSynthetic || !isSyntheticMock(c))
            .flatMap(c -> Stream.of(c.getDeclaredMethods()))
            .toList();
    }

    public static List<Field> getAllFields(Class<?> clazz, boolean includeMockitoSynthetic) {
        return getSuperClassesIncludingSelf(clazz).stream()
            .filter(c -> includeMockitoSynthetic || !isSyntheticMock(c))
            .flatMap(c -> Stream.of(c.getDeclaredFields()))
            .toList();
    }

    public static Class<?> getLambdaBaseClass(Class<?> clazz){
        if (!isLambda(clazz)){
            throw new IllegalArgumentException(clazz.getName()  + " is not a Lambda!");
        }

        return clazz.getInterfaces()[0];
    }

    public static boolean isLambda(Class<?> clazz) {
        return clazz.isSynthetic() && clazz.getDeclaredMethods().length == 1 && !clazz.getDeclaredMethods()[0].isSynthetic() && clazz.getSuperclass().equals(Object.class) && clazz.getInterfaces().length == 1;
    }

    public static boolean isObjectMethod(Method methodToCheck) {
        List<String> objectMethods =
            List.of("getClass", "hashCode", "equals", "clone", "toString", "notify", "notifyAll", "wait", "finalize");
        return objectMethods.contains(methodToCheck.getName());
    }

    public static Object callMethod(Object invoked, Method method, Object... arguments) {
        try {
            method.setAccessible(true);
            return method.invoke(invoked, arguments);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Context context = contextBuilder()
                .add("Object", invoked)
                .add(
                    "Parameters", Arrays.stream(arguments)
                        .map(o -> o != null ? o.toString() : o)
                        .toList()
                )
                .add(
                    "Stacktrace", ReflectionUtilsP.formatStackTrace(e)
                )
                .build();
            throw new StudentImplementationException(e, context, "Method " + method.getName() + "() can not be accessed or invoked with the supplied arguments!");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof AssertionFailedError) {
                throw new StudentImplementationException(cause, null, null);
            }
            if (cause instanceof StudentImplementationException sie) {
                throw sie;
            }

            Context context = contextBuilder()
                .add("Object", invoked)
                .add(
                    "Parameters", Arrays.stream(arguments)
                        .map(o -> o != null ? o.toString() : o)
                        .toList()
                )
                .add("Exception Class", cause.getClass())
                .add("Message", cause.getMessage())
                .add("Stacktrace", formatStackTrace(cause))
                .build();

            throw new StudentImplementationException(cause, context, "Method " + method.getName() + "() threw an exception!");
        }
    }

    public static @NotNull Method getMethodForParameters(String methodName, Class<?> clazz, List<Object> parameters)
        throws NoSuchMethodException {
        return getMethodForParameters(methodName, clazz, parameters, true);
    }

    public static @NotNull Method getMethodForParameters(String methodName, Class<?> clazz, List<Object> parameters,
                                                         boolean includeMockitoSynthetic) throws NoSuchMethodException {
        return getAllMethods(clazz, includeMockitoSynthetic).stream()
            .filter(m -> m.getName().equals(methodName))
            .filter(m -> {
                    List<Class<?>> parameterClasses =
                        parameters.stream().map(Object::getClass).map(Primitives::unwrap).collect(Collectors.toList());
                    List<Class<?>> actualParameters =
                        Arrays.stream(m.getParameters()).map(Parameter::getType).map(Primitives::unwrap).collect(Collectors.toList());

                    Map<Class<?>, Integer> occurrencesActual =
                        actualParameters.stream().collect(Collectors.groupingBy(
                            obj -> obj,
                            Collectors.summingInt(obj -> 1)
                        ));
                    Map<Class<?>, Integer> occurrencesExpected =
                        parameterClasses.stream().collect(Collectors.groupingBy(
                            obj -> obj == null ? MethodReference.class : obj,
                            Collectors.summingInt(obj -> 1)
                        ));

                    boolean hasCorrectParams = parameterClasses.stream().allMatch(
                        param -> actualParameters.stream().anyMatch(
                            actualParam -> param == null || (actualParam.isAssignableFrom(param)
                                && occurrencesActual.getOrDefault(actualParam, 0)
                                >= occurrencesExpected.getOrDefault(param, 0))
                        )
                    );
                    return parameters.size() == actualParameters.size() && hasCorrectParams;
                }
            )
            .findFirst()
            .orElseThrow(() -> {
                String methodsWithSameName = getAllMethods(clazz, false).stream()
                    .filter(m -> m.getName().equals(methodName))
                    .map(Objects::toString)
                    .collect(
                        Collectors.joining("\n"));
                if (methodsWithSameName.isBlank()) {
                    methodsWithSameName = "None";
                }
                return new NoSuchMethodException("Could not find method " + methodName + " with parameters " + parameters.stream()
                    .map(Object::getClass)
                    .map(Primitives::unwrap)
                    .toList() + ". Methods with same name:\n" + methodsWithSameName);
            });
    }

    public static Set<Package> getAllPackagesInExercise(Class<?> classInExercise) {
        if (packages != null) {
            return packages;
        }

        if (TestCycleResolver.getTestCycle() == null) {
            String dir = null;
            try {
                dir = new File(classInExercise.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
            } catch (URISyntaxException e2) {
                e2.printStackTrace();
            }

            try (Stream<@NotNull Path> paths = Files.walk(Paths.get(dir))) {

                Set<Package> packages = paths.toList().stream()
                    .map(Objects::toString)
                    .filter(path -> path.contains("java/main/") && path.endsWith(".class"))
                    .map(path -> path.split("java/main/")[1])
                    .map(path -> path.substring(0, path.lastIndexOf(File.separator)))
                    .collect(Collectors.toSet())
                    .stream()
                    .map(packageName -> classInExercise.getClassLoader().getDefinedPackage(packageName))
                    .collect(Collectors.toSet());

                ReflectionUtilsP.packages = packages;

                final var resourcePath = "src/graderPrivate/resources/packages.txt";
                File file = new File(resourcePath);
                file.getParentFile().mkdirs();
                file.createNewFile();

                Files.writeString(file.toPath(), packages.stream().map(Package::getName).collect(Collectors.joining("\n")));

                return packages;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Set<Package> packages =
            new BufferedReader(new InputStreamReader(ReflectionUtilsP.class.getResourceAsStream("/packages.txt")))
                .lines()
                .map(packageName -> classInExercise.getClassLoader().getDefinedPackage(packageName))
                .collect(Collectors.toSet());
        ReflectionUtilsP.packages = packages;

        return packages;

    }

    public static PackageLink getPackageLink(String pack) {
        PackageLink packageLink;
        if (TestCycleResolver.getTestCycle() != null) {
            List classes =
                new BufferedReader(new InputStreamReader(ReflectionUtilsP.class.getResourceAsStream("/classes/%s.txt".formatted(
                    pack))))
                    .lines()
                    .map(className -> {
                        try {
                            return Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            try {
                Constructor<BasicPackageLink> constructor =
                    BasicPackageLink.class.getDeclaredConstructor(String.class, Collection.class);
                constructor.setAccessible(true);
                packageLink = constructor.newInstance(
                    pack, classes);
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            packageLink = BasicPackageLink.of(pack);

            try {

                final var resourcePath = "src/graderPrivate/resources/classes/%s.txt".formatted(pack);
                File file = new File(resourcePath);
                file.getParentFile().mkdirs();
                file.createNewFile();

                Files.writeString(
                    file.toPath(),
                    packageLink.getTypes()
                        .stream()
                        .map(TypeLink::reflection)
                        .map(Class::getName)
                        .collect(Collectors.joining("\n"))
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return packageLink;
    }

    public static String formatStackTrace(Throwable exception) {
        String stacktrace = Arrays.stream(exception.getStackTrace())
            .map(Object::toString)
            .takeWhile(s -> !s.contains("java.base") && !s.contains("org.junit.jupiter"))
            .collect(Collectors.joining("\n                 "));
        if (stacktrace.isBlank()) {
            stacktrace = Arrays.stream(exception.getStackTrace())
                .map(Object::toString)
                .takeWhile(s -> !s.contains("org.junit.jupiter"))
                .dropWhile(s -> s.contains("java.base"))
                .collect(Collectors.joining("\n                 "));
        }
        return stacktrace;
    }

    public static String getExercisePrefix(Class<?> classInExercise) {
        Pattern pattern = Pattern.compile("^(?<exercise>h[a-zA-Z0-9]{2,})\\..*");
        Matcher matcher = pattern.matcher(classInExercise.getName());

        //needed for match to work. return may be ignored
        matcher.matches();

        return matcher.group("exercise");
    }

    public static Method stringToMethod(String methodString) {
        String[] entryPointStringSplits = methodString.split("#");
        String clazz = entryPointStringSplits[0];
        String[] method = entryPointStringSplits[1].replaceFirst(".$", "").split("\\(");

        try {
            return Class.forName(clazz).getDeclaredMethod(
                method[0], Arrays.stream(method).skip(1).map(param -> {
                    try {
                        return Class.forName(param);
                    } catch (ClassNotFoundException e) {
                        return ReflectionUtilsP.getClassFromPrimitiveString(param);
                    }
                }).toArray(Class[]::new)
            );
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String methodToString(Method method){
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String methodParameters = Arrays.stream(method.getParameters())
            .map(Parameter::getType)
            .map(Class::getName)
            .collect(Collectors.joining(", "));
        return className + "#" + methodName + "(" + methodParameters + ")";
    }
}
