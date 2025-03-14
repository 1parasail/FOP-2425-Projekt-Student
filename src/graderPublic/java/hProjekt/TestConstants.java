package hProjekt;

import java.util.concurrent.ThreadLocalRandom;

public class TestConstants {
    public static long RANDOM_SEED = ThreadLocalRandom.current().nextLong();
    public static final boolean SHOW_WORLD = java.lang.management.ManagementFactory
        .getRuntimeMXBean()
        .getInputArguments()
        .toString()
        .contains("-agentlib:jdwp"); // true if debugger is attached
    public static final int WORLD_DELAY = 500;

    public static final int TEST_TIMEOUT_IN_SECONDS = 10;

    public static final int TEST_ITERATIONS = 30;

    public static final boolean SKIP_AFTER_FIRST_FAILED_TEST = true;
}
