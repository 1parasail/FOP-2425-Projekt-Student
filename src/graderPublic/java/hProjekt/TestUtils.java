package hProjekt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hProjekt.controller.AiController;
import hProjekt.model.HexGrid;
import hProjekt.model.Player;
import hProjekt.model.PlayerImpl;
import javafx.scene.paint.Color;
import org.tudalgo.algoutils.tutor.general.SpoonUtils;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static hProjekt.TestConstants.RANDOM_SEED;

public abstract class TestUtils {

    /**
     * A generator for JSON test data.
     */
    public interface JsonGenerator {
        /**
         * Generates a JSON object node.
         *
         * @param mapper The object mapper to use.
         * @param index  The index of the object node.
         * @param rnd    The random number generator to use.
         * @return The generated JSON object node.
         */
        ObjectNode generateJson(ObjectMapper mapper, int index, Random rnd);
    }

    /**
     * Generates and saves JSON test data.
     *
     * @param generator The generator to use.
     * @param amount    The amount of test data to generate.
     * @param fileName  The file name to save the test data to.
     * @throws IOException If an I/O error occurs.
     */
    public static void generateJsonTestData(final JsonGenerator generator, final int amount, final String fileName) throws IOException {
        final var seed = RANDOM_SEED;
        final var random = new java.util.Random(seed);
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode arrayNode = mapper.createArrayNode();
        System.out.println("Generating test data with seed: " + seed);
        for (int i = 0; i < amount; i++) {
            arrayNode.add(generator.generateJson(mapper, i, random));
        }

        // convert `ObjectNode` to pretty-print JSON
//        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode));

        final var path = Paths.get(
            "src",
            "graderPublic",
            "resources",
            "hProjekt",
            fileName
        ).toAbsolutePath();
        System.out.printf("Saving to file: %s%n", path);
        final var file = path.toFile();
        file.createNewFile();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
    }

    /**
     * Returns the Spoon representation of the given method.
     *
     * @param clazz      the method's owner
     * @param methodName the method name
     * @param paramTypes the method's formal parameter types, if any
     * @return the Spoon representation of the given method
     */
    public static CtMethod<?> getCtMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return SpoonUtils.getType(clazz.getName())
            .getMethodsByName(methodName)
            .stream()
            .filter(ctMethod -> {
                List<CtParameter<?>> parameters = ctMethod.getParameters();
                boolean result = parameters.size() == paramTypes.length;
                for (int i = 0; result && i < parameters.size(); i++) {
                    result = parameters.get(i).getType().getQualifiedName().equals(paramTypes[i].getTypeName());
                }
                return result;
            })
            .findAny()
            .orElseThrow();
    }

    /**
     * Applies the given consumer to the body and its descendants of the given method.
     * See also: {@link #getCtMethod(Class, String, Class[])}.
     *
     * @param clazz      the method's owner
     * @param methodName the method name
     * @param paramTypes the method's formal parameter types, if any
     * @param consumer   the consumer to apply
     */
    public static void iterateMethodStatements(Class<?> clazz, String methodName, Class<?>[] paramTypes, Consumer<Iterator<CtElement>> consumer) {
        Iterator<CtElement> iterator = getCtMethod(clazz, methodName, paramTypes)
            .getBody()
            .descendantIterator();
        consumer.accept(iterator);
    }

    public static Player newPlayerInstance(final HexGrid hexGrid,
                                      final Color color,
                                      final int id,
                                      final String name,
                                      final Class<? extends AiController> ai) {
        try {
            Constructor<PlayerImpl> constructor = PlayerImpl.class.getDeclaredConstructor(HexGrid.class, Color.class, int.class, String.class, Class.class);
            constructor.setAccessible(true);
            return constructor.newInstance(hexGrid, color, id, name, ai);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
