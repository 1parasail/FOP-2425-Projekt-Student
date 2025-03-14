package hProjekt;

import static org.tudalgo.algoutils.tutor.general.jagr.RubricUtils.criterion;
import static org.tudalgo.algoutils.tutor.general.jagr.RubricUtils.graderPrivateOnly;

import org.sourcegrade.jagr.api.rubric.Criterion;
import org.sourcegrade.jagr.api.rubric.JUnitTestRef;
import org.sourcegrade.jagr.api.rubric.Rubric;
import org.sourcegrade.jagr.api.rubric.RubricProvider;
import org.tudalgo.algoutils.tutor.general.jagr.RubricUtils;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;

import com.fasterxml.jackson.databind.node.ObjectNode;

import hProjekt.controller.GameControllerTest;
import hProjekt.controller.LeaderboardControllerTests;
import hProjekt.controller.PlayerControllerTest;
import hProjekt.model.EdgeImplTest;
import hProjekt.model.HexGridImplTest;
import hProjekt.model.PlayerImplTests;
import hProjekt.model.TileImplTest;

public class HProjekt_RubricProviderPublic implements RubricProvider {

    private static final Criterion HProjekt_1_1 = Criterion.builder()
            .shortDescription("P1.1 | Daten des Players")
            .maxPoints(1)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methoden getHexGrid, getname, getID, getColor und isAi geben für einen Spieler die korrekten Werte zurück.",
                            JUnitTestRef.ofMethod(() -> PlayerImplTests.class.getDeclaredMethod("testGetHexGrid")),
                            JUnitTestRef.ofMethod(() -> PlayerImplTests.class.getDeclaredMethod("testGetName")),
                            JUnitTestRef.ofMethod(() -> PlayerImplTests.class.getDeclaredMethod("testGetId")),
                            JUnitTestRef.ofMethod(() -> PlayerImplTests.class.getDeclaredMethod("testGetColor")),
                            JUnitTestRef.ofMethod(
                                    () -> PlayerImplTests.class.getDeclaredMethod("testIsAi", boolean.class))))
            .build();

    private static final Criterion HProjekt_1_2 = Criterion.builder()
            .shortDescription("P1.2 | Bankkonto des Players")
            .maxPoints(2)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode getCredits gibt die korrekte Anzahl Credits eines Spielers zurück.",
                            JUnitTestRef.ofMethod(
                                    () -> PlayerImplTests.class.getDeclaredMethod("testGetCredits", int.class))),
                    criterion(
                            "Die Methoden addCredits und removeCredits sind vollständig korrekt implementiert.",
                            JUnitTestRef.ofMethod(
                                    () -> PlayerImplTests.class.getDeclaredMethod("testAddCredits", int.class)),
                            JUnitTestRef.ofMethod(
                                    () -> PlayerImplTests.class.getDeclaredMethod("testRemoveCredits", int.class)),
                            JUnitTestRef.ofMethod(
                                    () -> PlayerImplTests.class.getDeclaredMethod("testRemoveCreditsNegative"))))
            .build();

    private static final Criterion HProjekt_1_3 = Criterion.builder()
            .shortDescription("P1.3 | Alle Schienen führen nach ...")
            .maxPoints(6)
            .minPoints(0)
            .addChildCriteria(
                    privateCriterion(
                            "Die Methode getRails gibt eine unveränderbare Sicht auf die Schienen des Spielers zurück."),
                    criterion(
                            "Die Methode connectsTo gibt genau dann true zurück, wenn beide Edges ein gemeinsames Tile besitzen.",
                            JUnitTestRef.ofMethod(
                                    () -> EdgeImplTest.class.getDeclaredMethod("testConnectsTo", ObjectNode.class))),
                    criterion(
                            "Die Methode getConnectedEdges gibt alle Kanten der, durch die Edge verbundenen, Tiles zurück.",
                            JUnitTestRef.ofMethod(
                                    () -> EdgeImplTest.class.getDeclaredMethod("testGetConnectedEdges",
                                            ObjectNode.class))),
                    privateCriterion(
                            "Die Methode getConnectedRails gibt alle Schienen zurück, die an den beiden Tiles, die die Edge verbindet, anliegen und zum gegebenen Spieler gehören."),
                    criterion(
                            "Die Methode addRail gibt true zurück und fügt eine Schiene hinzu, genau dann wenn alle Kriterien zum Bauen erfüllt sind.",
                            2,
                            JUnitTestRef.ofMethod(
                                    () -> EdgeImplTest.class.getDeclaredMethod("testAddRail", ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_1_4 = Criterion.builder()
            .shortDescription("P1.4 | ...Rom - Implementation der Städte")
            .maxPoints(11)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode getNeighbour gibt das Tile zurück, welches in der richtigen Richtung angrenzt.",
                            JUnitTestRef.ofMethod(
                                    () -> TileImplTest.class.getDeclaredMethod("testGetNeighbour",
                                            ObjectNode.class))),
                    criterion(
                            "Die Methode getEdge gibt die Edge zurück, welche in der richtigen Richtung das Tile verbindet.",
                            JUnitTestRef.ofMethod(
                                    () -> TileImplTest.class.getDeclaredMethod("testGetEdge",
                                            ObjectNode.class))),
                    criterion(
                            "Die Methode getConnectedNeighbours gibt die korrekten Tiles zurück.",
                            3,
                            JUnitTestRef.ofMethod(
                                    () -> TileImplTest.class.getDeclaredMethod("testGetConnectedNeighbours",
                                            ObjectNode.class))),
                    criterion(
                            "Die Methode getConnectedCities gibt eine unveränderbare Sicht auf alle verbundenen Städte zurück.",
                            2,
                            JUnitTestRef.ofMethod(
                                    () -> HexGridImplTest.class.getDeclaredMethod("testGetConnectedCities",
                                            ObjectNode.class))),
                    criterion(
                            "Die Methode getUnconnectedCities gibt eine unveränderbare Sicht auf alle noch nicht verbundenen Städte zurück.",
                            2,
                            JUnitTestRef.ofMethod(
                                    () -> HexGridImplTest.class.getDeclaredMethod("testGetUnconnectedCities",
                                            ObjectNode.class))),
                    criterion("Die Methode getStartingCities gibt alle Startstädte zurück.",
                            2, JUnitTestRef.ofMethod(
                                    () -> HexGridImplTest.class.getDeclaredMethod("testGetStartingCities",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_1 = Criterion.builder()
            .shortDescription("P1 | Implementierung des Modells")
            .minPoints(0)
            .addChildCriteria(
                    HProjekt_1_1,
                    HProjekt_1_2,
                    HProjekt_1_3,
                    HProjekt_1_4)
            .build();

    private static final Criterion HProjekt_2_1 = Criterion.builder()
            .shortDescription("P2.1 | Welche Gleise?")
            .maxPoints(4)
            .minPoints(0)
            .addChildCriteria(
                    criterion("Die Methode canBuildRail ist vollständig und korrekt implementiert.", 3,
                            JUnitTestRef.ofMethod(
                                    () -> PlayerControllerTest.class.getDeclaredMethod("testCanBuildRail",
                                            ObjectNode.class))),
                    criterion(
                            "Die Methode getBuildableRails ist vollständig und korrekt implementiert.",
                            JUnitTestRef.ofMethod(
                                    () -> PlayerControllerTest.class.getDeclaredMethod("testGetBuildableRails",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_2 = Criterion.builder()
            .shortDescription("P2.2 | Gleise bauen")
            .maxPoints(4)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode buildRail ist vollständig und korrekt implementiert.", 4,
                            JUnitTestRef.ofMethod(
                                    () -> PlayerControllerTest.class.getDeclaredMethod("testBuildRail",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_3 = Criterion.builder()
            .shortDescription("P2.3 | Städte verbinden und bauen")
            .maxPoints(4)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode executeBuildingPhase ist vollständig und korrekt implementiert.", 4,
                            JUnitTestRef.ofMethod(
                                    () -> GameControllerTest.class.getDeclaredMethod("testExecuteBuildingPhase",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_4 = Criterion.builder()
            .shortDescription("P2.4 | Prepare the race")
            .maxPoints(2)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode chooseCities ist vollständig und korrekt implementiert.", 2,
                            JUnitTestRef.ofMethod(
                                    () -> GameControllerTest.class.getDeclaredMethod("testChooseCities",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_5 = Criterion.builder()
            .shortDescription("P2.5 | Darf ich fahren?")
            .maxPoints(4)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode canDrive ist vollständig und korrekt implementiert.", 1, JUnitTestRef.ofMethod(
                                    () -> PlayerControllerTest.class.getDeclaredMethod("testCanDrive",
                                            ObjectNode.class))),
                    criterion("Die Methode drive ist vollständig und korrekt implementiert.", 3,
                            JUnitTestRef.ofMethod(
                                    () -> PlayerControllerTest.class.getDeclaredMethod("testDrive",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_6 = Criterion.builder()
            .shortDescription("P2.6 | Die Qual der Wahl, welche Strecke nehmen ich denn?")
            .maxPoints(2)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode letPlayersChoosePath ist vollständig und korrekt implementiert.", 2,
                            JUnitTestRef.ofMethod(
                                    () -> GameControllerTest.class.getDeclaredMethod("testLetPlayersChoosePath",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_7 = Criterion.builder()
            .shortDescription("P2.7 | Tchooo, Tchooo: Steuerung des Fahrens im Spiel")
            .maxPoints(3)
            .minPoints(0)
            .addChildCriteria(
                    manualCriterion(
                            "Die Methode handleDriving ist vollständig und korrekt implementiert.", 3))
            .build();

    private static final Criterion HProjekt_2_8 = Criterion.builder()
            .shortDescription("P2.8 | Winner, Winner, Chicken-Dinner")
            .maxPoints(2)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode getWinners ist vollständig und korrekt implementiert.", 2,
                            JUnitTestRef.ofMethod(
                                    () -> GameControllerTest.class.getDeclaredMethod("testGetWinners",
                                            ObjectNode.class))))
            .build();

    private static final Criterion HProjekt_2_9 = Criterion.builder()
            .shortDescription("P2.9 | Let the race begin!")
            .maxPoints(3)
            .minPoints(0)
            .addChildCriteria(
                    manualCriterion(
                            "Die Methode executeDrivingPhase ist vollständig und korrekt implementiert.", 3))
            .build();

    private static final Criterion HProjekt_2 = Criterion.builder()
            .shortDescription("P2 | Control the Flow!")
            .minPoints(0)
            .addChildCriteria(
                    HProjekt_2_1,
                    HProjekt_2_2,
                    HProjekt_2_3,
                    HProjekt_2_4,
                    HProjekt_2_5,
                    HProjekt_2_6,
                    HProjekt_2_7,
                    HProjekt_2_8,
                    HProjekt_2_9)
            .build();

    private static final Criterion HProjekt_3_1 = Criterion.builder()
            .shortDescription("P3.1 | Leaderboard-Daten speichern")
            .maxPoints(3)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode savePlayerData verwendet das richtige CSV-Format.",
                            JUnitTestRef.ofMethod(() -> LeaderboardControllerTests.class
                                    .getDeclaredMethod("testSavePlayerData_csvFormat", JsonParameterSet.class))),
                    criterion(
                            "Die Methode savePlayerData speichert den Namen, die Punkte und den Spielertyp korrekt in einer CSV-Datei.",
                            JUnitTestRef.ofMethod(() -> LeaderboardControllerTests.class
                                    .getDeclaredMethod("testSavePlayerData_dataCorrect", JsonParameterSet.class))),
                    criterion(
                            "Die aktuelle Zeit wird korrekt formatiert.",
                            JUnitTestRef.ofMethod(() -> LeaderboardControllerTests.class
                                    .getDeclaredMethod("testSavePlayerData_timestampFormat", JsonParameterSet.class))))
            .build();

    private static final Criterion HProjekt_3_2 = Criterion.builder()
            .shortDescription("P3.2 | Leaderboard-Daten laden")
            .maxPoints(3)
            .minPoints(0)
            .addChildCriteria(
                    criterion(
                            "Die Methode loadLeaderboardData ignoriert die Kopfzeile der CSV-Datei und modifiziert die Datei nicht.",
                            JUnitTestRef.ofMethod(() -> LeaderboardControllerTests.class
                                    .getDeclaredMethod("testLoadLeaderboardData_noMod", JsonParameterSet.class))),
                    criterion(
                            "Die Methode loadLeaderboardData liest alle gültigen Einträge aus der CSV-Datei und gibt sie als Liste von LeaderBoardEntry zurück.",
                            2,
                            JUnitTestRef.ofMethod(() -> LeaderboardControllerTests.class
                                    .getDeclaredMethod("testLoadLeaderboardData_dataCorrect", JsonParameterSet.class))))
            .build();

    private static final Criterion HProjekt_3 = Criterion.builder()
            .shortDescription("P3 | Highscore!")
            .minPoints(0)
            .addChildCriteria(
                    HProjekt_3_1,
                    HProjekt_3_2)
            .build();

    private static final Criterion HProjekt_4_1 = Criterion.builder()
            .shortDescription("P4.1 | Should I stay or should I go?")
            .maxPoints(1)
            .minPoints(0)
            .addChildCriteria(
                    privateCriterion(
                            "Die Methode updateUIBasedOnObjective ist vollständig und korrekt implementiert."))
            .build();

    private static final Criterion HProjekt_4_2 = Criterion.builder()
            .shortDescription("P4.2 | Wo ist der Weg?")
            .maxPoints(5)
            .minPoints(0)
            .addChildCriteria(
                    privateCriterion(
                            "Die Methode trimPath verkürzt den Pfad korrekt und gibt alle Kanten des Pfads zurück.",
                            4),
                    privateCriterion(
                            "Die Methode highlightPath hebt alle übergebenen Edges korrekt hervor."))
            .build();

    private static final Criterion HProjekt_4_3 = Criterion.builder()
            .shortDescription("P4.3 | Wo solls eigentlich losgehen?")
            .maxPoints(5)
            .minPoints(0)
            .addChildCriteria(
                    privateCriterion(
                            "Die Methode highlightStartingTiles ist vollständig und korrekt implementiert.", 5))
            .build();

    private static final Criterion HProjekt_4_4 = Criterion.builder()
            .shortDescription("P4.4 | Bob der Baumeister: Wir bauen Schienen!")
            .maxPoints(5)
            .minPoints(0)
            .addChildCriteria(
                    privateCriterion(
                            "Die Methode addBuildHandlers ist vollständig und korrekt implementiert.", 5))
            .build();

    private static final Criterion HProjekt_4 = Criterion.builder()
            .shortDescription("P4 | Dem User Interface etwas Leben einhauchen")
            .minPoints(0)
            .addChildCriteria(
                    HProjekt_4_1,
                    HProjekt_4_2,
                    HProjekt_4_3,
                    HProjekt_4_4)
            .build();

    private static final Criterion HProjekt_5_1 = Criterion.builder()
            .shortDescription("P5.1 | Neue Spielmechaniken")
            .maxPoints(15)
            .minPoints(0)
            .addChildCriteria(
                    manualCriterion(
                            "Die erste neue Spielmechanik wurde in das Spiel integriert und ist verständlich dokumentiert.",
                            1),
                    manualCriterion(
                            "Die zweite neue Spielmechanik wurde in das Spiel integriert und ist verständlich dokumentiert.",
                            1),
                    manualCriterion(
                            "Die erste neue Spielmechanik bereichert das Spiel und ist nicht zu stark oder zu schwach.",
                            1),
                    manualCriterion(
                            "Die zweite neue Spielmechanik bereichert das Spiel und ist nicht zu stark oder zu schwach.",
                            1),
                    manualCriterion(
                            "Je nach Komplexität der ersten Mechanik können hier noch bis zu fünf weitere Punkte vergeben werden.",
                            5),
                    manualCriterion(
                            "Je nach Komplexität der zweiten Mechanik können hier noch bis zu fünf weitere Punkte vergeben werden.",
                            5),
                    manualCriterion(
                            "Es wurden 2 Spielmechaniken implementiert.",
                            1))
            .build();

    private static final Criterion HProjekt_5_2 = Criterion.builder()
            .shortDescription("P5.2 | KI als Gegner?")
            .maxPoints(15)
            .minPoints(0)
            .addChildCriteria(
                    manualCriterion(
                            "Die Strategie des Computergegners ist gut dokumentiert und sinnvoll.", 7),
                    manualCriterion(
                            "Die Strategie des Computergegners ist komplexer, als die der vorgegebenen KI.",
                            1),
                    manualCriterion(
                            "Der Computergegner ist implementiert und kann über das Menü ausgewählt werden.",
                            1),
                    manualCriterion(
                            "Der Computergegner kann alle Aktionen sinnvoll ausführen.",
                            3),
                    manualCriterion(
                            "Der Computergegner führt nur erlaubte Aktionen aus",
                            1),
                    manualCriterion(
                            "Wenn man zwei Computergegner gegeneinander spielen lässt, gewinnt einer der beiden.",
                            2))
            .build();

    private static final Criterion HProjekt_5 = Criterion.builder()
            .shortDescription("P5 | Weiterführende Aufgaben")
            .minPoints(0)
            .addChildCriteria(
                    HProjekt_5_1,
                    HProjekt_5_2)
            .build();

    public static final Rubric RUBRIC = Rubric.builder()
            .title("Projekt | Dampfross")
            .addChildCriteria(
                    HProjekt_1,
                    HProjekt_2,
                    HProjekt_3,
                    HProjekt_4,
                    HProjekt_5)
            .build();

    public static Criterion privateCriterion(String message) {
        return privateCriterion(message, 0, 1);
    }

    public static Criterion privateCriterion(String message, int max) {
        return privateCriterion(message, 0, max);
    }

    public static Criterion privateCriterion(String message, int min, int max) {
        return Criterion.builder()
                .shortDescription(message)
                .grader(graderPrivateOnly(max))
                .minPoints(min)
                .maxPoints(max)
                .build();
    }

    public static Criterion manualCriterion(String message) {
        return manualCriterion(message, 0, 1);
    }

    public static Criterion manualCriterion(String message, int max) {
        return manualCriterion(message, 0, max);
    }

    public static Criterion manualCriterion(String message, int min, int max) {
        return Criterion.builder()
                .shortDescription(message)
                .grader(RubricUtils
                        .manualGrader(max))
                .minPoints(min)
                .maxPoints(max)
                .build();
    }

    @Override
    public Rubric getRubric() {
        return RUBRIC;
    }
}
