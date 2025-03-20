package hProjekt.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import hProjekt.controller.actions.IllegalActionException;
import hProjekt.controller.actions.RollDiceAction;
import hProjekt.model.*;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import hProjekt.Config;
import hProjekt.controller.actions.ConfirmBuildAction;
import hProjekt.controller.actions.PlayerAction;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Pair;

/**
 * The GameController class represents the controller for the game logic.
 * It manages the game state, player controllers, dice rolling and the overall
 * progression of the game.
 * It tells the players controllers what to do and when to do it.
 */
public class GameController {
    private final GameState state;
    private final Map<Player, PlayerController> playerControllers;
    private final List<AiController> aiControllers = new ArrayList<>();
    private final Supplier<Integer> dice;
    private final IntegerProperty currentDiceRoll = new SimpleIntegerProperty(0);
    private final IntegerProperty roundCounter = new SimpleIntegerProperty(0);
    private Property<Pair<City, City>> chosenCitiesProperty = new SimpleObjectProperty<>();

    private final Property<PlayerController> activePlayerController = new SimpleObjectProperty<>();

    private boolean stopped = false;

    /**
     * Creates a new GameController with the given game state and dice supplier.
     *
     * @param state the game state
     * @param dice  the dice supplier
     */
    public GameController(GameState state, Supplier<Integer> dice) {
        this.state = state;
        this.playerControllers = new HashMap<>();
        this.dice = dice;
    }

    /**
     * Creates a new GameController with the given game state.
     *
     * @param state the game state
     */
    public GameController(GameState state) {
        this(state, () -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1));
    }

    /**
     * Creates a new GameController with a new game state and a random dice
     * supplier.
     */
    public GameController() {
        this(new GameState(new HexGridImpl(Config.TOWN_NAMES), new ArrayList<>()),
                () -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1));
    }

    /**
     * Returns the game state.
     *
     * @return the game state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Returns a map from players to player controllers.
     *
     * @return a map from players to player controllers
     */
    public Map<Player, PlayerController> getPlayerControllers() {
        return playerControllers;
    }

    /**
     * Returns a property that contains the active player controller.
     *
     * @return a property that contains the active player controller
     */
    public Property<PlayerController> activePlayerControllerProperty() {
        return activePlayerController;
    }

    /**
     * Returns the active player controller.
     *
     * @return the active player controller
     */
    public PlayerController getActivePlayerController() {
        return activePlayerController.getValue();
    }

    /**
     * Returns the current dice roll property.
     *
     * @return the current dice roll property
     */
    public IntegerProperty currentDiceRollProperty() {
        return currentDiceRoll;
    }

    /**
     * Returns the current dice roll.
     *
     * @return the current dice roll
     */
    public int getCurrentDiceRoll() {
        return currentDiceRoll.get();
    }

    /**
     * Returns the round counter property.
     *
     * @return the round counter property
     */
    public IntegerProperty roundCounterProperty() {
        return roundCounter;
    }

    /**
     * Returns the chosen cities property that contains the starting and target city
     * as a javaFX Pair.
     *
     * @return the chosen cities property
     */
    public ReadOnlyProperty<Pair<City, City>> chosenCitiesProperty() {
        return chosenCitiesProperty;
    }

    /**
     * Returns the starting city.
     *
     * @return the starting city
     */
    public City getStartingCity() {
        return chosenCitiesProperty.getValue().getKey();
    }

    /**
     * Returns the target city.
     *
     * @return the target city
     */
    public City getTargetCity() {
        return chosenCitiesProperty.getValue().getValue();
    }

    /**
     * Casts the dice and returns the result.
     *
     * @return the result of the dice roll
     */
    public int castDice() {
        currentDiceRoll.set(dice.get());
        return currentDiceRoll.get();
    }

    /**
     * Stops the game and the Thread.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Initializes the player controllers for each player in the game state.
     * If a player is an AI, it creates an AI controller for the player.
     */
    private void initPlayerControllers() {
        for (Player player : state.getPlayers()) {
            playerControllers.put(player, new PlayerController(this, player));
            if (player.isAi()) {
                try {
                    aiControllers.add(player.getAiController()
                            .getConstructor(PlayerController.class, HexGrid.class, GameState.class, Property.class,
                                    IntegerProperty.class, IntegerProperty.class, ReadOnlyProperty.class)
                            .newInstance(playerControllers.get(player), state.getGrid(), state,
                                    activePlayerController, currentDiceRoll, roundCounter, chosenCitiesProperty));
                } catch (NoSuchMethodException e) {
                    System.err.println("Could not create ai controller for player " + player.getName());
                    System.err.println("You probably forgot to implement the constructor in your ai controller.");
                    System.err.println("The full error message: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    System.err.println("Could not create ai controller for player " + player.getName());
                    System.err.println("You probably do not have all necessary parameters in your constructor.");
                    System.err.println("The full error message: " + e.getMessage());
                    e.printStackTrace();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.err.println("Could not create ai controller for player " + player.getName());
                    System.err.println("An error occurred while trying to create the ai controller.");
                    System.err.println("The full error message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Starts the game and handles the game loop.
     *
     * The game consists of two phases: the building phase and the driving phase.
     *
     * @throws IllegalStateException if there are not enough playerss
     */
    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        if (playerControllers.isEmpty()) {
            initPlayerControllers();
        }

        // Bauphase
        getState().getGamePhaseProperty().setValue(GamePhase.BUILDING_PHASE);
        executeBuildingPhase();

        // Fahrphase
        getState().getGamePhaseProperty().setValue(GamePhase.DRIVING_PHASE);
        roundCounter.set(0);
        executeDrivingPhase();

        getState().getWinnerProperty().setValue(getState().getPlayers().stream()
                .max((p1, p2) -> Integer.compare(p1.getCredits(), p2.getCredits())).get());
    }

    /**
     * Executes the building phase of the game.
     * The building phase consists of the following steps:
     * - While there are unconnected cities, let a player roll the dice
     * - Starting with the player that rolled the dice, let the players build until
     * all players have built
     * - The players are given a building budget according to the dice roll
     * - Repeat until there are only
     * {@link Config#UNCONNECTED_CITIES_START_THRESHOLD} unconnected cities left
     */
    @StudentImplementationRequired("P2.3")
    private void executeBuildingPhase() {
        // TODO: P2.3

        int countOfUnconnectedCities = getState().getGrid().getUnconnectedCities().size();

        while (countOfUnconnectedCities >= Config.UNCONNECTED_CITIES_START_THRESHOLD)
        {
            roundCounter.set(roundCounter.get()+1);

            int indexOfPlayer = (roundCounter.get()-1) % state.getPlayers().size();

            Player playerOfRound = state.getPlayers().get(indexOfPlayer);

            PlayerController playerControllerOfPlayerOfRound = getPlayerControllers().get(playerOfRound);

            playerControllerOfPlayerOfRound.setPlayerObjective(PlayerObjective.ROLL_DICE);

            int resOfDice = castDice();

            for (PlayerController playerController : getPlayerControllers().values())
            {
                playerController.setBuildingBudget(resOfDice);
                waitForBuild(playerController);

                Set<Edge> buildableRails = playerControllerOfPlayerOfRound.getBuildableRails();

                for (Edge buildableRail : buildableRails)
                {
                    try
                    {
                        playerControllerOfPlayerOfRound.buildRail(buildableRail);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not build rail " + buildableRail);
                    }
                }

            }
            countOfUnconnectedCities = getState().getGrid().getUnconnectedCities().size();
        }
    }

    /**
     * Chooses two random cities from the grid and sets them as starting and target
     * city.
     * The chosen cities are stored in the chosen cities property.
     */
    @StudentImplementationRequired("P2.4")
    public void chooseCities() {
        // TODO: P2.4


            Map<TilePosition, City> cities = getState().getGrid().getCities();

            List<TilePosition> cityPositions = new ArrayList<>(cities.keySet());

            int randomStartCity = Config.RANDOM.nextInt(cityPositions.size());
            City startCity = cities.get(cityPositions.get(randomStartCity));

            int randomForFinishCity = Config.RANDOM.nextInt(cityPositions.size());
            City finishCity = cities.get(cityPositions.get(randomForFinishCity));

            if (finishCity.equals(startCity) == true) {
                while (finishCity.equals(startCity) == true) {
                    randomForFinishCity = Config.RANDOM.nextInt(cityPositions.size());
                    finishCity = cities.get(cityPositions.get(randomForFinishCity));
                }
            }

            getState().addChosenCity(startCity);
            getState().addChosenCity(finishCity);

            chosenCitiesProperty = new SimpleObjectProperty<>(new Pair<>(startCity, finishCity));
        }


    /**
     * Let the players build during the driving phase.
     * The players are sorted by their credits in ascending order ensuring that the
     * player with the least credits builds first.
     * Players are given a fixed building budget of
     * {@link Config#MAX_BUILDINGBUDGET_DRIVING_PHASE}.
     */
    private void buildingDuringDrivingPhase() {
        getState().getPlayers().stream().sorted((p1, p2) -> Integer.compare(p1.getCredits(), p2.getCredits()))
                .forEachOrdered((player) -> {
                    final PlayerController pc = playerControllers.get(player);
                    pc.setBuildingBudget(Config.MAX_BUILDINGBUDGET_DRIVING_PHASE);
                    waitForBuild(pc);
                });
    }

    /**
     * Let the players choose the rails they want to rent and confirm the calculated
     * path.
     */
    @StudentImplementationRequired("P2.6")
    private void letPlayersChoosePath() {
        // TODO: P2.6
        Map<Player, PlayerController> playerControllers = getPlayerControllers();

        for (Map.Entry<Player, PlayerController> entry : playerControllers.entrySet())
        {
            PlayerController playerController = entry.getValue();
            Player player = entry.getKey();

            playerController.resetDrivingPhase();
            getState().setPlayerPositon(player, getStartingCity().getPosition());

            playerController.waitForNextAction(PlayerObjective.CHOOSE_PATH);
            playerController.setPlayerObjective(PlayerObjective.CHOOSE_PATH);
            playerController.waitForNextAction(PlayerObjective.CONFIRM_PATH);
            playerController.setPlayerObjective(PlayerObjective.CONFIRM_PATH);



        }
    }

    /**
     * Handles the driving.
     * If only one player is driving, the player automatically reaches the target
     * city.
     * While there are players that have not reached the target city and there are
     * still credits to win, let the players roll the dice and drive.
     * If a player reaches the target city, all other players surplus will be
     * reduced by {@link Config#DICE_SIDES} before each round.
     * The players are sorted by their credits in descending order ensuring that the
     * player with the most credits drives first.
     */
    @StudentImplementationRequired("P2.7")
    private void handleDriving() {
        // TODO: P2.7

        List<Player> drivingPlayers = getState().getDrivingPlayers();

        if (drivingPlayers.size()==0)
        {
            return;
        }

        if (drivingPlayers.size()==1)
        {

            for (int i = 0; i < drivingPlayers.size(); i++)
            {
                Player player = drivingPlayers.get(i);
                getState().setPlayerPositon(player, getTargetCity().getPosition());
                getState().setWinner(player);
            }
            return;
        }

        List<Player> finishedPlayers = new ArrayList<>();

        while(finishedPlayers.size() < Config.WINNING_CREDITS.size() || finishedPlayers.size() < drivingPlayers.size())
        {
            Map<Player, TilePosition> positions = getState().getPlayerPositions();

            for (Map.Entry<Player, TilePosition> entry : positions.entrySet())
            {
                Player player = entry.getKey();
                TilePosition position = entry.getValue();

                for (int i = 0; i < drivingPlayers.size(); i++)
                {
                    if (player.equals(drivingPlayers.get(i))==true)
                    {
                        if (position.equals(getTargetCity().getPosition())==false)
                        {
                            drivingPlayers.get(i).removeCredits(Config.DICE_SIDES);
                        }
                        else
                        {
                            finishedPlayers.add(drivingPlayers.get(i));
                            drivingPlayers.remove(i);
                        }
                    }

                }
            }

            drivingPlayers.sort(Comparator.comparingInt(Player::getCredits));

            Map<Player, PlayerController> playerControllers = getPlayerControllers();

            for (Map.Entry<Player, PlayerController> entry : playerControllers.entrySet())
            {
                Player player = entry.getKey();
                PlayerController playerController = entry.getValue();

                for (int i = 0; i < drivingPlayers.size(); i++)
                {
                    if (player.equals(drivingPlayers.get(i))==true)
                    {
                        playerController.setPlayerObjective(PlayerObjective.ROLL_DICE);
                        playerController.setPlayerObjective(PlayerObjective.DRIVE);
                    }
                }
            }
        }
    }

    /**
     * Returns the winners of a round.
     * The winners are the players that have reached the target city. If multiple
     * players have reached the target city, the player with the biggest point
     * surplus wins.
     *
     * @return the winners of a round
     */
    @StudentImplementationRequired("P2.8")
    private List<Player> getWinners() {
        // TODO: P2.8
        List<Player> drivingPlayers = getState().getDrivingPlayers();
        Map<Player, TilePosition> positionMap = getState().getPlayerPositions();
        List<Player> winners = new ArrayList<>();

        for (Map.Entry<Player, TilePosition> entry : positionMap.entrySet())
        {
            Player player = entry.getKey();
            TilePosition position = entry.getValue();

            for (int i = 0; i < drivingPlayers.size(); i++)
            {
                if (player.equals(drivingPlayers.get(i))==true)
                {
                    if(position.equals(getTargetCity().getPosition())==true)
                    {
                        winners.add(player);
                    }
                }
            }
        }

        int difference = winners.size() - Config.WINNING_CREDITS.size();

        for (int i = 0; i < difference; i++)
        {
            winners.remove(winners.size()-1);
        }

        Map<Player, Integer> playersPointSurplus = getState().getPlayerPointSurplus();
        Map<Player, Integer> sortedPlayersPointSurplus = (Map<Player, Integer>) playersPointSurplus.entrySet().stream().sorted((point1, point2) -> Integer.compare(point2.getValue(), point1.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (point1, point2)->point1));

        for (Map.Entry<Player, Integer> entry : sortedPlayersPointSurplus.entrySet())
        {
            Player player = entry.getKey();

            for (int i = 0; i < winners.size(); i++)
            {
                winners.set(i,player);
            }
        }
        return winners;
    }

    /**
     * Executes the driving phase of the game.
     * The driving phase consists of the following steps:
     * - If the round counter is a multiple of 3, let the players build during the
     * driving phase
     * - Let a player choose the cities to drive to
     * - Let the players choose their path
     * - Let the players that are driving roll the dice and drive
     * - Check if a player has reached the target city and if so, add credits to the
     * player
     * - Repeat until all cities were chosen
     */
    @StudentImplementationRequired("P2.9")
    private void executeDrivingPhase() {
        // TODO: P2.9
        while(getState().getChosenCities().size()==0)
        {
            int countOfRounds = roundCounter.get();
            roundCounter.set(countOfRounds+1);

            getState().resetDrivingPlayers();
            getState().resetPlayerPositions();
            getState().resetPlayerSurplus();

            if (roundCounter.get() % 3 == 0)
            {
                buildingDuringDrivingPhase();
            }

            int indexOfPlayer = (roundCounter.get()-1) % (getState().getPlayers().size());
            PlayerController controllerOfPlayer = getPlayerControllers().get(indexOfPlayer);
            controllerOfPlayer.setPlayerObjective(PlayerObjective.CHOOSE_CITIES);

            this.letPlayersChoosePath();
            this.handleDriving();

            List<Player> winners = getWinners();

            for (int i = 0; i < winners.size(); i++)
            {
                winners.get(i).addCredits(Config.WINNING_CREDITS.get(i));
            }
        }
    }

    /**
     * Waits for the player to build.
     *
     * @param pc The {@link PlayerController} to wait for.
     */
    private void waitForBuild(final PlayerController pc) {
        withActivePlayer(pc, () -> {
            PlayerAction action = pc.waitForNextAction(PlayerObjective.PLACE_RAIL);
            while (!(action instanceof ConfirmBuildAction)) {
                action = pc.waitForNextAction();
            }
        });
    }

    /**
     * Executes the given {@link Runnable} and set the active player to the given
     * {@link PlayerController}.
     * After the {@link Runnable} is executed, the active player is set to
     * {@code null} and the objective is set to {@link PlayerObjective#IDLE}.
     *
     * @param pc The {@link PlayerController} to set as active player.
     * @param r  The {@link Runnable} to execute.
     */
    @DoNotTouch
    public void withActivePlayer(final PlayerController pc, final Runnable r) {
        if (stopped) {
            throw new RuntimeException("Game was stopped");
        }
        activePlayerController.setValue(pc);
        r.run();
        pc.setPlayerObjective(PlayerObjective.IDLE);
        activePlayerController.setValue(null);
    }
}
