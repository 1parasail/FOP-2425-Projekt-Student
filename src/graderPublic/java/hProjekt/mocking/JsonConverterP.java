package hProjekt.mocking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hProjekt.MockExclude;
import kotlin.Pair;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static hProjekt.mocking.ReflectionUtilsP.actsLikePrimitive;
import static hProjekt.mocking.ReflectionUtilsP.getFieldValue;
import static hProjekt.mocking.ReflectionUtilsP.getSuperClassesIncludingSelf;
import static hProjekt.mocking.ReflectionUtilsP.methodToString;
import static hProjekt.mocking.ReflectionUtilsP.setFieldValue;
import static hProjekt.mocking.ReflectionUtilsP.stringToMethod;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

public class JsonConverterP {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    protected Map<Class<?>, Function<Object, JsonNode>> typeMapperJSON = new HashMap<>() {{
        put(List.class, (list) -> toJsonNode((List<?>) list));
        put(Set.class, (list) -> toJsonNode((Set<?>) list));
        put(Map.class, (map) -> toJsonNode((Map<?, ?>) map));
        put(
            Entry.class,
            (entry) -> MAPPER.createArrayNode()
                .add(toJsonNode(((Entry) entry).getKey()))
                .add(toJsonNode(((Entry) entry).getValue()))
        );
        put(Method.class, (method) -> MAPPER.createObjectNode().put("name", methodToString((Method) method)));
        put(Class.class, (clazz) -> MAPPER.createObjectNode().put("name", ((Class<?>)clazz).getName()));
    }};

    protected Map<Class<?>, BiFunction<JsonNode, Answer<?>, Object>> typeMapperObject = new HashMap<>() {{
        put(List.class, (node, answer) -> toList(node, answer));
        put(Set.class, (node, answer) -> toSet(node, answer));
        put(Map.class, (node, answer) -> toMap(node, answer));
        put(Entry.class, (node, answer) -> toEntry(node, answer));
        put(Method.class, ((node, answer) -> stringToMethod(node.get("name").asText())));
        put(Class.class, ((node, answer) -> {
            try {
                return Class.forName(node.get("name").asText());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
    }};

    public <T> List<T> toList(final JsonNode jsonNode, Answer<?> defaultAnswer) {
        if (jsonNode instanceof ArrayNode arrayNode && arrayNode.isEmpty()) {
            return new hProjekt.mocking.ArrayList<>();
        }

        AtomicBoolean successful = new AtomicBoolean(true);
        List<T> rtn = (List<T>) StreamSupport.stream(jsonNode.spliterator(), false)
            .map(node -> {
                try {
                    return fromJsonNode((ObjectNode) node, defaultAnswer);
                } catch (RuntimeException e) {
                    successful.set(false);
                }
                return null;
            })
            .collect(Collectors.toCollection(hProjekt.mocking.ArrayList::new));
        if (successful.get()) {
            return rtn;
        }
        throw new IllegalStateException("Could not Create List. Inner Object failed to be created!");
    }

    public <T> Set<T> toSet(final JsonNode jsonNode, Answer<?> defaultAnswer) {
        if (jsonNode instanceof ArrayNode arrayNode && arrayNode.isEmpty()) {
            return new ArraySet<>();
        }
        List<Throwable> successful = new ArrayList<>();
        Set<T> rtn = (Set<T>) StreamSupport.stream(jsonNode.spliterator(), false)
            .map(node -> {
                try {
                    return fromJsonNode((ObjectNode) node, defaultAnswer);
                } catch (RuntimeException e) {
                    successful.add(e);
                }
                return new Object();
            })
            .collect(Collectors.toCollection(ArraySet::new));
        if (successful.isEmpty()) {
            return rtn;
        }
        throw new IllegalStateException("Could not Create Set. Inner Object failed to be created!", successful.getFirst());
    }

    public <K, V> Map<K, V> toMap(final JsonNode jsonNode, Answer<?> defaultAnswer) {
        if (jsonNode instanceof ArrayNode arrayNode && arrayNode.isEmpty()) {
            return new NonHashMap<>();
        }
        List<Throwable> successful = new ArrayList<>();
        Map<K, V> returnMap = new NonHashMap<>();
        var pairs = StreamSupport.stream(jsonNode.spliterator(), false)
            .map(node -> {
                Object key = null;
                Object value = null;
                try {
                    key = fromJsonNode((ObjectNode) node.get(0), defaultAnswer);
                } catch (RuntimeException e) {
                    successful.add(e);
                }
                try {
                    value = fromJsonNode((ObjectNode) node.get(1), defaultAnswer);
                } catch (RuntimeException e) {
                    successful.add(e);
                }

                return new Pair<>(key, value);
            })
            .toList();
        pairs.forEach(entry -> returnMap.put((K) entry.component1(), (V) entry.component2()));
        if (successful.isEmpty()) {
            return returnMap;
        }
        throw new IllegalStateException("Could not Create Map. Inner Object failed to be created!", successful.getFirst());
    }

    public <K, V> Map.Entry<K, V> toEntry(final JsonNode jsonNode, Answer<?> defaultAnswer) {

        boolean successful = true;
        K key = null;
        V value = null;
        try {
            key = fromJsonNode((ObjectNode) jsonNode.get(0), defaultAnswer);
        } catch (RuntimeException e) {
            successful = false;
        }
        try {
            value = fromJsonNode((ObjectNode) jsonNode.get(1), defaultAnswer);
        } catch (RuntimeException e) {
            successful = false;
        }
        if (successful) {
            return Map.entry(key, value);
        }
        throw new IllegalStateException("Could not Create Entry. Inner Object failed to be created!");
    }

    public ObjectNode toJsonNode(Object toMap) {
        ObjectNode rootNode = MAPPER.createObjectNode();

        if (toMap == null) {
            rootNode.put("type", "null");
            return rootNode;
        }
//        System.out.println("Mapping Object of class: " + toMap.getClass() + " with Identity: " + System.identityHashCode(toMap));

        Class<?> objectClass = toMap.getClass();

        //test if class is a lambda
        if (ReflectionUtilsP.isLambda(objectClass)) {
            rootNode.put("type", objectClass.getInterfaces()[0].getName());
        } else if (ReflectionUtilsP.isSyntheticMock(objectClass)) {
            rootNode.put("type", objectClass.getInterfaces()[0].getName());
        } else {
            rootNode.put("type", objectClass.getName());
        }


        if (toMap.getClass().isArray()) {
            List<Object> elements = new ArrayList<>(Array.getLength(toMap));

            for (int i = 0; i < Array.getLength(toMap); i++){
                elements.add(Array.get(toMap, i));
            }

            toMap = elements;
        }

        if (actsLikePrimitive(toMap.getClass())) {
            rootNode.put("value", toMap.toString());
            return rootNode;
        } else if (getSuperClassesIncludingSelf(toMap.getClass()).stream().anyMatch(type -> typeMapperJSON.containsKey(type))) {
            Class<?> foundClass = getSuperClassesIncludingSelf(toMap.getClass()).stream()
                .filter(type -> typeMapperJSON.containsKey(type))
                .findFirst()
                .orElseThrow();
            rootNode.set(
                "value",
                typeMapperJSON.get(foundClass).apply(toMap)
            );
            rootNode.put("type", foundClass.getName());
            return rootNode;
        }

        ArrayNode fieldsJSON = MAPPER.createArrayNode();

        //Skip fields for mocks
        if (!ReflectionUtilsP.isSyntheticMock(objectClass)) {
            List<Field> fields = ReflectionUtilsP.getSuperClassesIncludingSelf(objectClass).stream().flatMap(clazz -> Arrays.stream(clazz.getDeclaredFields())).toList().reversed();

            for (Field field : fields) {
                if (filterFields(field)) {
                    continue;
                }

                ObjectNode fieldJSON = MAPPER.createObjectNode();

                String name = field.getName();
                Object fieldValue = getFieldValue(toMap, name);

                fieldJSON.put("name", name);

                for (Map.Entry<String, JsonNode> value : toJsonNode(fieldValue).properties()) {
                    fieldJSON.set(value.getKey(), value.getValue());
                }

                fieldsJSON.add(fieldJSON);
            }
        }

        rootNode.set("fields", fieldsJSON);

        return rootNode;
    }

    protected boolean filterFields(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return true;
        }
        if (field.isSynthetic()){
            return true;
        }
        if (Arrays.stream(field.getAnnotations()).anyMatch(MockExclude.class::isInstance) ){
            return true;
        }
        if (Throwable.class.isAssignableFrom(field.getDeclaringClass()) && field.getName().equals("stackTrace")){
            return true;
        }
        return false;
    }

    public static <T> Class<T> getTypeFromNode(ObjectNode nodeToConvert) {
        String className = nodeToConvert.get("type").asText();

        if (className.equals("null")) {
            return null;
        }

        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromJsonNode(ObjectNode nodeToConvert, Answer<?> defaultAnswer) {
        Class<T> objectClass = getTypeFromNode(nodeToConvert);

        if (objectClass == null) {
            return null;
        }

        //needs to be here for primitive parsing
        if (actsLikePrimitive(objectClass)) {
            return extractPrimitiveValue(objectClass, nodeToConvert);
        } else if (getSuperClassesIncludingSelf(objectClass).stream().anyMatch(type -> typeMapperObject.containsKey(type))) {
            return (T) typeMapperObject.get(getSuperClassesIncludingSelf(objectClass).stream()
                .filter(type -> typeMapperObject.containsKey(type))
                .findFirst()
                .orElseThrow()).apply(nodeToConvert.get("value"), defaultAnswer);
        }

        T constructed;
        if (defaultAnswer != null) {
            constructed = mock(objectClass, defaultAnswer);
        } else {
            try {
                constructed = (T) ReflectionUtilsP.getUnsafe().allocateInstance(objectClass);
            } catch (InstantiationException e) {
                constructed = mock(objectClass, CALLS_REAL_METHODS);
            }
        }

        for (JsonNode fieldNode : nodeToConvert.get("fields")) {
            try {
                setFieldValues(defaultAnswer, (ObjectNode) fieldNode, constructed);
            } catch (RuntimeException e) {
                System.err.println("Could not parse " + fieldNode);
                e.printStackTrace();
            }
        }
        return constructed;
    }

    protected <T> void setFieldValues(Answer<?> defaultAnswer, ObjectNode fieldNode, T constructed) {
        String fieldName = fieldNode.get("name").asText();

        try {
            Object fieldValue = fromJsonNode(fieldNode, defaultAnswer);

            if (fieldValue != null) {
                setFieldValue(constructed, fieldName, fieldValue);
            }
        } catch (Exception e) {
            ReflectionUtilsP.getUnsafe().throwException(e);
        }
    }

    public static <T> T extractPrimitiveValue(Class<T> objectClass, ObjectNode node) {
        String value = node.get("value").asText();
        Object fieldValue = null;

        if (String.class == objectClass) {
            fieldValue = value;
        } else if (boolean.class == objectClass || Boolean.class == objectClass) {
            fieldValue = Boolean.parseBoolean(value);
        } else if (byte.class == objectClass || Byte.class == objectClass) {
            fieldValue = Byte.parseByte(value);
        } else if (short.class == objectClass || Short.class == objectClass) {
            fieldValue = Short.parseShort(value);
        } else if (int.class == objectClass || Integer.class == objectClass) {
            fieldValue = Integer.parseInt(value);
        } else if (long.class == objectClass || Long.class == objectClass) {
            fieldValue = Long.parseLong(value);
        } else if (float.class == objectClass || Float.class == objectClass) {
            fieldValue = Float.parseFloat(value);
        } else if (double.class == objectClass || Double.class == objectClass) {
            fieldValue = Double.parseDouble(value);
        } else if (char.class == objectClass || Character.class == objectClass) {
            fieldValue = value.charAt(0);
        } else if (Enum.class.isAssignableFrom(objectClass)) {
            try {
                return (T) objectClass.getDeclaredMethod("valueOf", String.class).invoke(null, node.get("value").asText());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return (T) fieldValue;
    }

    public JsonNode toJsonNode(List<?> toMap) {
        ArrayNode rootNode = MAPPER.createArrayNode();
        for (Object object : toMap) {
            rootNode.add(toJsonNode(object));
        }
        return rootNode;
    }

    public JsonNode toJsonNode(Set<?> toMap) {
        ArrayNode rootNode = MAPPER.createArrayNode();
        for (Object object : toMap) {
            rootNode.add(toJsonNode(object));
        }
        return rootNode;
    }

    public JsonNode toJsonNode(Map<?, ?> toMap) {
        ArrayNode rootNode = MAPPER.createArrayNode();
        for (Map.Entry<?, ?> object : toMap.entrySet()) {
            ArrayNode vals = MAPPER.createArrayNode();
            vals.add(toJsonNode(object.getKey()));
            vals.add(toJsonNode(object.getValue()));
            rootNode.add(vals);
        }
        return rootNode;
    }
}
