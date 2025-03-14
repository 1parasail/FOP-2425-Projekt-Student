package hProjekt.model;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import hProjekt.Config;
import javafx.beans.property.Property;
import javafx.util.Pair;

/**
 * Default implementation of {@link Edge}.
 *
 * @param grid       the HexGrid instance this edge is placed in
 * @param position1  the first position
 * @param position2  the second position
 * @param railOwners the road's owner, if a road has been built on this edge
 */
public record EdgeImpl(HexGrid grid, TilePosition position1, TilePosition position2, Property<List<Player>> railOwners)
        implements Edge {
    @Override
    public HexGrid getHexGrid() {
        return grid;
    }

    @Override
    public TilePosition getPosition1() {
        return position1;
    }

    @Override
    public TilePosition getPosition2() {
        return position2;
    }

    @Override
    public Property<List<Player>> getRailOwnersProperty() {
        return railOwners;
    }

    @Override
    @StudentImplementationRequired("P1.3")
    public Set<Edge> getConnectedRails(final Player player) {
        // TODO: P1.3
       Map<Set<TilePosition>, Edge> rails = player.getRails();

       Set<Edge> connectedRails = new HashSet<>();

       for (Map.Entry<Set<TilePosition>, Edge> entry : rails.entrySet())
       {
           Edge edge = entry.getValue();

           if (edge.equals(this)==true)
           {
               connectedRails.add(edge);
           }
       }

       return connectedRails;


    }

    @Override
    public Map<Player, Integer> getRentingCost(Player player) {
        if (getRailOwners().contains(player)) {
            return Map.of();
        }
        return getRailOwners().stream().collect(Collectors.toMap(p -> p, p -> 1));
    }

    @Override
    public int getDrivingCost(TilePosition from) {
        if (!getAdjacentTilePositions().contains(from)) {
            throw new IllegalArgumentException("The given position is not adjacent to this edge.");
        }
        return Config.TILE_TYPE_TO_DRIVING_COST.get(new Pair<Tile.Type, Tile.Type>(
                getHexGrid().getTileAt(from).getType(),
                getHexGrid().getTileAt(getPosition1().equals(from) ? getPosition2() : getPosition1()).getType()));
    }

    @Override
    public int getTotalBuildingCost(Player player) {
        return getBaseBuildingCost() + getTotalParallelCost(player);
    }

    @Override
    public int getTotalParallelCost(Player player) {
        return getParallelCostPerPlayer(player).values().stream().reduce(0, Integer::sum);
    }

    @Override
    public Map<Player, Integer> getParallelCostPerPlayer(Player player) {
        final Map<Player, Integer> result = new HashMap<>();
        if ((!getRailOwners().isEmpty()) && (!((getRailOwners().size() == 1) && getRailOwners().contains(player)))) {
            if (Collections.disjoint(getHexGrid().getCities().keySet(), getAdjacentTilePositions())) {
                getRailOwners().stream().forEach(p -> result.put(p, 5));
            } else {
                getRailOwners().stream().forEach(p -> result.put(p, 3));
            }
        }
        getAdjacentTilePositions().stream().flatMap(position -> {
            if (getHexGrid().getCityAt(position) != null) {
                return Stream.empty();
            }
            Set<Player> owners = getHexGrid().getTileAt(position).getEdges().stream()
                    .filter(Predicate.not(this::equals)).flatMap(edge -> edge.getRailOwners().stream())
                    .collect(Collectors.toUnmodifiableSet());
            if (owners.contains(player)) {
                return Stream.empty();
            }
            return owners.stream();
        }).forEach(p -> result.put(p, Math.max(result.getOrDefault(p, 0), 1)));
        return result;
    }

    @Override
    public int getBaseBuildingCost() {
        return Config.TILE_TYPE_TO_BUILDING_COST.get(getAdjacentTilePositions().stream()
                .map(position -> getHexGrid().getTileAt(position).getType()).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public List<Player> getRailOwners() {
        return getRailOwnersProperty().getValue();
    }

    @Override
    public boolean removeRail(Player player) {
        return getRailOwnersProperty().getValue().remove(player);
    }

    @Override
    @StudentImplementationRequired("P1.3")
    public boolean addRail(Player player) {
        // TODO: P1.3
        Map<Set<TilePosition>, Edge> rails = player.getRails();

        boolean added = false;

        if (rails!=null) {

            if (this.hasRail() == false) {
               if (player.getHexGrid().getCityAt(this.getPosition1())!=null || player.getHexGrid().getCityAt(this.getPosition2())!=null)
               {
                   if (player.getHexGrid().getCityAt(this.getPosition1()).isStartingCity()==true || player.getHexGrid().getCityAt(this.getPosition2()).isStartingCity()==true)
                   {
                       added = true;
                   }
               }
               else
               {
                   added = false;
               }
            }
            else {
                Set<Edge> connectedRails = getConnectedRails(player);
                for (Edge edge : connectedRails) {
                    if (this.connectsTo(edge) == true) {
                        added = true;
                        break;
                    }
                }
            }
        }
        if (added == true)
        {
            Set<TilePosition> positions = new HashSet<>();
            positions.add(this.getPosition1());
            positions.add(this.getPosition2());
            rails.put(positions, this);
        }

        return added;
    }

    @Override
    public boolean hasRail() {
        return (getRailOwnersProperty().getValue() != null) && (!getRailOwnersProperty().getValue().isEmpty());
    }

    @Override
    @StudentImplementationRequired("P1.3")
    public boolean connectsTo(Edge other) {
        // TODO: P1.3
       if (this.getPosition1().equals(other.getPosition1())==true || this.getPosition2().equals(other.getPosition2())==true)
       {
           return true;
       }
       else
       {
           return false;
       }
    }

    @Override
    public Set<TilePosition> getAdjacentTilePositions() {
        return Set.of(getPosition1(), getPosition2());
    }

    @Override
    @StudentImplementationRequired("P1.3")
    public Set<Edge> getConnectedEdges() {
        // TODO: P1.3
        Set<TilePosition> adjacentTilePositions = getAdjacentTilePositions();
        Set<Edge> connectedEdges = new HashSet<>();
        for (TilePosition adjacentTilePosition : adjacentTilePositions)
        {
            Tile adjacentTile = getHexGrid().getTileAt(adjacentTilePosition);
            if (adjacentTile!=null) {
                Set<Edge> edges = adjacentTile.getEdges();

                for (Edge edge : edges) {
                    if (this.connectsTo(edge) == true) {
                        connectedEdges.add(edge);
                    }
                }
            }
        }

        return connectedEdges;
    }
}
