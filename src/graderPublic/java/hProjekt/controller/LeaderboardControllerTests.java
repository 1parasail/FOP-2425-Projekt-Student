package hProjekt.controller;

import hProjekt.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class LeaderboardControllerTests {

    private static Path orignalPath;

    @BeforeAll
    public static void init() throws IOException {
        orignalPath = Config.CSV_PATH;
        Config.CSV_PATH = Files.createTempFile(null, null);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Config.CSV_PATH);
        Config.CSV_PATH = orignalPath;
    }

    @BeforeEach
    public void setup() throws IOException {
        Files.deleteIfExists(Config.CSV_PATH);
    }

    @ParameterizedTest
    @JsonParameterSetTest("/hProjekt/LeaderboardCsvData.generated.json")
    public void testSavePlayerData_csvFormat(JsonParameterSet params) throws IOException {
        List<Map<String, ?>> entries = params.get("entries");
        writeCsvEntries(entries, true);

        List<String> writtenLines = Files.readAllLines(Config.CSV_PATH);
        assertEquals(entries.size() + 1, writtenLines.size(), emptyContext(), r ->
            "The number of lines written does not match the number of calls to savePlayerData(String, int, boolean) + 1 for header");
        assertEquals("PlayerName,AI,Timestamp,Score", writtenLines.getFirst(), emptyContext(), r ->
            "The first line does not match the expected header string");
        for (int i = 1; i < writtenLines.size(); i++) {
            final int finalI = i;
            assertTrue(writtenLines.get(i).matches("^[a-zA-Z0-9]+,(true|false),[^,]+,\\d+$"), emptyContext(), r ->
                "Line " + finalI + " does not match the expected format");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/hProjekt/LeaderboardCsvData.generated.json")
    public void testSavePlayerData_dataCorrect(JsonParameterSet params) {
        List<Map<String, ?>> entries = params.get("entries");
        writeCsvEntries(entries, true);

        List<Map<String, ?>> writtenEntries = readCsvEntries();
        assertEquals(entries.size(), writtenEntries.size(), emptyContext(), r ->
            "The number of entries written does not match the number of calls to savePlayerData(String, int, boolean)");
        for (int i = 0; i < entries.size(); i++) {
            final int entryNum = i + 1;
            Map<String, ?> expected = entries.get(i);
            Map<String, ?> actual = writtenEntries.get(i);

            assertEquals(expected.get("player"), actual.get("player"), emptyContext(), r ->
                "Entry %d does not have the correct player name".formatted(entryNum));
            assertEquals(expected.get("ai"), actual.get("ai"), emptyContext(), r ->
                "Entry %d does not have the correct AI flag".formatted(entryNum));
            assertEquals(expected.get("score"), actual.get("score"), emptyContext(), r ->
                "Entry %d does not have the correct score".formatted(entryNum));
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/hProjekt/LeaderboardCsvData.generated.json")
    public void testSavePlayerData_timestampFormat(JsonParameterSet params) {
        List<Map<String, ?>> entries = params.get("entries");
        writeCsvEntries(entries, true);

        List<Map<String, ?>> writtenEntries = readCsvEntries();
        assertEquals(entries.size(), writtenEntries.size(), emptyContext(), r ->
            "The number of entries written does not match the number of calls to savePlayerData(String, int, boolean)");
        for (int i = 0; i < entries.size(); i++) {
            final int entryNum = i + 1;
            assertTrue(((String) writtenEntries.get(i).get("timestamp")).matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$"),
                emptyContext(),
                r -> "Entry %d does not have the correct timestamp format".formatted(entryNum));
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/hProjekt/LeaderboardCsvData.generated.json")
    public void testLoadLeaderboardData_noMod(JsonParameterSet params) throws IOException {
        List<Map<String, ?>> entries = params.get("entries");
        writeCsvEntries(entries, false);
        String expectedHash = DigestUtils.md5Hex(Files.readAllBytes(Config.CSV_PATH));

        List<LeaderboardEntry> leaderboardEntries = callObject(LeaderboardController::loadLeaderboardData, emptyContext(), r ->
            "An exception occurred while invoking loadLeaderboardData()");
        String actualHash = DigestUtils.md5Hex(Files.readAllBytes(Config.CSV_PATH));
        assertEquals(entries.size(), leaderboardEntries.size(), emptyContext(), r ->
            "The number of entries in the returned list does not match the number of entries in the csv file (excluding header)");
        assertEquals(expectedHash, actualHash, emptyContext(), r ->
            "The file was modified by loadLeaderboardData() - The MD5 hashes do not match");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/hProjekt/LeaderboardCsvData.generated.json")
    public void testLoadLeaderboardData_dataCorrect(JsonParameterSet params) {
        List<Map<String, ?>> entries = params.get("entries");
        writeCsvEntries(entries, false);

        List<LeaderboardEntry> leaderboardEntries = callObject(LeaderboardController::loadLeaderboardData, emptyContext(), r ->
            "An exception occurred while invoking loadLeaderboardData()");
        assertEquals(entries.size(), leaderboardEntries.size(), emptyContext(), r ->
            "The number of entries in the returned list does not match the number of entries in the csv file");
        for (int i = 0; i < entries.size(); i++) {
            final int entryNum = i;
            Map<String, ?> expected = entries.get(i);
            LeaderboardEntry actual = leaderboardEntries.get(i);

            assertEquals(expected.get("player"), actual.getPlayerName(), emptyContext(), r ->
                "Entry %d in the returned list does not have the correct player name".formatted(entryNum));
            assertEquals(expected.get("ai"), actual.isAi(), emptyContext(), r ->
                "Entry %d in the returned list does not have the correct AI flag".formatted(entryNum));
            assertEquals(expected.get("timestamp"), actual.getTimestamp(), emptyContext(), r ->
                "Entry %d in the returned list does not have the correct timestamp".formatted(entryNum));
            assertEquals(expected.get("score"), actual.getScore(), emptyContext(), r ->
                "Entry %d in the returned list does not have the correct score".formatted(entryNum));
        }
    }

    private void writeCsvEntries(List<Map<String, ?>> entries, boolean useStudentImpl) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes("PlayerName,AI,Timestamp,Score\n".getBytes());

        for (Map<String, ?> entry : entries) {
            String player = (String) entry.get("player");
            boolean ai = (Boolean) entry.get("ai");
            String timestamp = (String) entry.get("timestamp");
            int score = (Integer) entry.get("score");

            if (useStudentImpl) {
                Context context = contextBuilder()
                    .add("playerName", player)
                    .add("ai", ai)
                    .add("score", score)
                    .build();

                call(() -> LeaderboardController.savePlayerData(player, score, ai), context, r ->
                    "An exception occurred while invoking savePlayerData(String, int, boolean)");
            } else {
                baos.writeBytes("%s,%b,%s,%d%n".formatted(player, ai, timestamp, score).getBytes());
            }
        }

        if (!useStudentImpl) {
            try {
                Files.write(Config.CSV_PATH, baos.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Map<String, ?>> readCsvEntries() {
        List<Map<String, ?>> entries = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Config.CSV_PATH)) {
            String line = reader.readLine(); // Skips the header row
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                String playerName = values[0];
                boolean ai = Boolean.parseBoolean(values[1]);
                String timestamp = values[2];
                int score = Integer.parseInt(values[3]);

                entries.add(Map.of(
                    "player", playerName,
                    "ai", ai,
                    "timestamp", timestamp,
                    "score", score
                ));
            }
        } catch (IOException e) {
            System.out.println("Error while reading the leaderboard csv file: " + e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }
}
