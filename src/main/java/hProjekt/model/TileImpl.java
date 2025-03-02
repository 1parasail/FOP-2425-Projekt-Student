package hProjekt.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import hProjekt.model.TilePosition.EdgeDirection;
import javafx.beans.value.ObservableDoubleValue;

/**
 * Holds information on a tile.
 *
 * @param position       this tile's position
 * @param type           the type of this tile
 * @param heightProperty the height of this tile
 * @param widthProperty  the width of this tile
 * @param hexGrid        the grid this tile is placed in
 * @see Tile
 * @see TilePosition
 */
public record TileImpl(TilePosition position, Type type, ObservableDoubleValue heightProperty,
        ObservableDoubleValue widthProperty, HexGrid hexGrid) implements Tile {
    /**
     * Alternative constructor with q- and r-coordinates instead of a
     * {@link TilePosition}.
     *
     * @param q              the q-coordinate of this tile in the grid
     * @param r              the r-coordinate of this tile in the grid
     * @param type           the type of this tile
     * @param heightProperty the height of this tile
     * @param widthProperty  the width of this tile
     * @param hexGrid        the grid this tile is placed in
     */
    @DoNotTouch
    public TileImpl(final int q, final int r, final Type type, final ObservableDoubleValue heightProperty,
            final ObservableDoubleValue widthProperty, final HexGrid hexGrid) {
        this(new TilePosition(q, r), type, heightProperty, widthProperty, hexGrid);
    }

    @Override
    public TilePosition getPosition() {
        return position;
    }

    @Override
    public HexGrid getHexGrid() {
        return hexGrid;
    }

    @Override
    @StudentImplementationRequired("P1.4")
    public Edge getEdge(final EdgeDirection direction) {
        // TODO: P1.4
        Set<Edge> edgesOfThis = this.getEdges();
        Edge edge = null;

        for (Edge e : edgesOfThis)
        {
            if (e.equals(direction)==true)
            {
                edge = e;
            }
        }

        return edge;



    }

    @Override
    public boolean hasCity() {
        return getHexGrid().getCityAt(position) != null;
    }

    @Override
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(
                EdgeDirection.stream().map(this::getEdge).filter(edge -> edge != null).collect(Collectors.toSet()));
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Tile> getNeighbours() {
        return getHexGrid().getTiles().entrySet().stream()
                .filter(entrySet -> TilePosition.neighbours(getPosition()).contains(entrySet.getKey()))
                .map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    @Override
    @StudentImplementationRequired("P1.4")
    public Tile getNeighbour(final EdgeDirection direction) {
        // TODO: P1.4
        Set<Edge> edges = this.getEdges();
        Set<TilePosition> tiles = new HashSet<>();

        for (Edge edge : edges)
        {
            if (edge.equals(direction)==true)
            {
                tiles.addAll(edge.getAdjacentTilePositions());
            }
        }
        Tile neighbour = null;

        for (TilePosition tile : tiles)
        {
            if(tile.equals(this.getPosition())==false)
            {
               neighbour = hexGrid.getTileAt(tile);
            }
        }

        return neighbour;
    }

    @Override
    public boolean isAtCoast() {
        return getNeighbours().size() < 6;
    }

    @Override
    @StudentImplementationRequired("P1.4")
    public Set<Tile> getConnectedNeighbours(Set<Edge> connectingEdges) {
        // TODO: P1.4
       Set<Tile> tiles = new HashSet<>();
       Set<TilePosition> tilesPositions = new HashSet<>();

       for (Edge edge : connectingEdges)
       {
           tilesPositions.addAll(edge.getAdjacentTilePositions());
           tilesPositions.remove(this.getPosition());
       }

       for (TilePosition tile : tilesPositions)
       {
           tiles.add(hexGrid.getTileAt(tile));
       }

       return tiles;
    }

    @Override
    public Set<Edge> getRails(Player player) {
        return getEdges().stream().filter(edge -> edge.getRailOwners().contains(player)).collect(Collectors.toSet());
    }
}
