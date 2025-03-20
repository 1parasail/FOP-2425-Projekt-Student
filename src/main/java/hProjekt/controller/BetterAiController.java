package hProjekt.controller;

import java.util.List;
import java.util.Set;

import hProjekt.Config;
import hProjekt.controller.actions.BuildRailAction;
import hProjekt.controller.actions.ChooseCitiesAction;
import hProjekt.controller.actions.ChooseRailsAction;
import hProjekt.controller.actions.ConfirmBuildAction;
import hProjekt.controller.actions.ConfirmDrive;
import hProjekt.controller.actions.DriveAction;
import hProjekt.controller.actions.PlayerAction;
import hProjekt.controller.actions.RollDiceAction;
import hProjekt.model.City;
import hProjekt.model.Edge;
import hProjekt.model.GameState;
import hProjekt.model.HexGrid;
import hProjekt.model.Tile;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.util.Pair;


/**
 * Better AI controller that makes strategic decisions instead of random ones.
 */
public class BetterAiController extends AiController {

    /**
     * Creates a new better AI controller with the given player controller, hex grid,
     * game state and active player controller.
     * Adds a subscription to the player objective property to execute actions when
     * the player's objective changes.
     *
     * @param playerController       the player controller
     * @param hexGrid                the hex grid
     * @param gameState              the game state
     * @param activePlayerController the active player controller
     */
    public BetterAiController(final PlayerController playerController, final HexGrid hexGrid, final GameState gameState,
                              final Property<PlayerController> activePlayerController, final IntegerProperty diceRollProperty,
                              final IntegerProperty roundCounterProperty, final ReadOnlyProperty<Pair<City, City>> chosenCitiesProperty) {
        super(playerController, hexGrid, gameState, activePlayerController, diceRollProperty, roundCounterProperty,
                chosenCitiesProperty);
    }

    /**
     * Executes better AI's actions based on the given objective.
     * @param objective the player objective
     */
    @Override
    protected void executeActionBasedOnObjective(PlayerObjective objective) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Main thread was interrupted", e);
        }

        final Set<Class<? extends PlayerAction>> allowedActions = playerController.getPlayerObjective()
            .getAllowedActions();

        if (allowedActions.contains(RollDiceAction.class)) {
            playerController.triggerAction(new RollDiceAction());
        }
        if (allowedActions.contains(BuildRailAction.class)) {
            buildBestRail();
        }
        if (allowedActions.contains(ConfirmBuildAction.class) && playerController.getPlayerState().buildableRailEdges()
            .isEmpty()) {
            playerController.triggerAction(new ConfirmBuildAction());
        }
        if (allowedActions.contains(ChooseCitiesAction.class)) {
            playerController.triggerAction(new ChooseCitiesAction());
        }
        if (allowedActions.contains(ChooseRailsAction.class)) {
            playerController.triggerAction(new ChooseRailsAction(Set.of()));
        }
        if (allowedActions.contains(ConfirmDrive.class)) {
            playerController.triggerAction(new ConfirmDrive(true));
        }
        if (allowedActions.contains(DriveAction.class)) {
            driveToTarget();
        }
    }

    private void buildBestRail() {
        List<Edge> buildableEdges = playerController.getPlayerState().buildableRailEdges().stream().toList();
        if (buildableEdges.isEmpty()) {
            return;
        }
        int budget = playerController.getPlayerState().buildingBudget();

        Edge bestEdge = buildableEdges.stream()
            .filter(e -> e.getTotalBuildingCost(playerController.getPlayer()) <= budget)
            .min((e1, e2) -> Integer.compare(e1.getTotalBuildingCost(playerController.getPlayer()), e2.getTotalBuildingCost(playerController.getPlayer())))
            .orElse(null);

        if (bestEdge != null) {
            playerController.triggerAction(new BuildRailAction(List.of(bestEdge)));
        }
    }

    private void driveToTarget() {
        List<Tile> drivableTiles = playerController.getPlayerState().drivableTiles().keySet().stream().toList();
        if (drivableTiles.isEmpty()) {
            return;
        }

        Tile bestTile = null;
        int shortestPath = Integer.MAX_VALUE;

        for (Tile tile : drivableTiles) {
            int pathLength = getPathLengthToTarget(tile);
            if (pathLength < shortestPath) {
                shortestPath = pathLength;
                bestTile = tile;
            }
        }

        if (bestTile != null) {
            playerController.triggerAction(new DriveAction(bestTile));
        }
    }

    private int getPathLengthToTarget(Tile tile) {
        List<Tile> path = playerController.getPlayerState().drivableTiles().get(tile);
        return (path != null) ? path.size() : Integer.MAX_VALUE; // Falls kein Pfad existiert, sehr hoher Wert
    }

}
