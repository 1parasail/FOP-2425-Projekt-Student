package hProjekt;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@DisabledIf("org.tudalgo.algoutils.tutor.general.Utils#isJagrRun()")
public class TestJsonGenerators {

    @Test
    public void generateLeaderboardCsvData() throws IOException {
        ZoneOffset offset = ZoneOffset.ofHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        long startTime = LocalDateTime.parse("2024-10-01T00:00").toEpochSecond(offset);
        long endTime = LocalDateTime.parse("2025-04-01T00:00").toEpochSecond(offset);

        TestUtils.generateJsonTestData(
            (mapper, index, rnd) -> {
                ObjectNode rootNode = mapper.createObjectNode();
                ArrayNode entries = mapper.createArrayNode();
                int entryNum = rnd.nextInt(1, 6);

                for (int i = 0; i < entryNum; i++) {
                    String playerName = "player" + i;
                    boolean ai = rnd.nextBoolean();
                    String timestamp = formatter.format(LocalDateTime.ofEpochSecond(rnd.nextLong(startTime, endTime), 0, offset));
                    int score = rnd.nextInt(100);

                    ObjectNode entry = mapper.createObjectNode()
                        .put("player", playerName)
                        .put("ai", ai)
                        .put("timestamp", timestamp)
                        .put("score", score);
                    entries.add(entry);
                }

                rootNode.set("entries", entries);
                return rootNode;
            },
            5,
            "LeaderboardCsvData.generated.json"
        );
    }
}
