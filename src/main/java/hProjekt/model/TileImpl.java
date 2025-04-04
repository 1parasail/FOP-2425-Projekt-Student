package hProjekt.model;

import java.util.*;
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

        Tile neighbour = this.getNeighbour(direction);
        if (neighbour == null)
        {
            return null;
        }

        return getHexGrid().getEdge(this.getPosition(), neighbour.getPosition());
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

        if (direction.equals(EdgeDirection.EAST)==true)
        {
             return this.getHexGrid().getTileAt(this.position.q()+1, this.position().r());
        }
        else if (direction.equals(EdgeDirection.NORTH_EAST)==true)
        {
            return this.getHexGrid().getTileAt(this.position.q()+1, this.position().r()-1);
        }
        else if (direction.equals(EdgeDirection.NORTH_WEST)==true)
        {
            return this.getHexGrid().getTileAt(this.position.q(), this.position().r()-1);
        }
        else if (direction.equals(EdgeDirection.WEST)==true)
        {
            return this.getHexGrid().getTileAt(this.position.q()-1, this.position().r());
        }
        else if (direction.equals(EdgeDirection.SOUTH_WEST)==true)
        {
            return  this.getHexGrid().getTileAt(this.position.q()-1, this.position().r()+1);
        }
        else if (direction.equals(EdgeDirection.SOUTH_EAST)==true)
        {
            return this.getHexGrid().getTileAt(this.position.q(), this.position().r()+1);
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean isAtCoast() {
        return getNeighbours().size() < 6;
    }

    @Override
    @StudentImplementationRequired("P1.4")
    public Set<Tile> getConnectedNeighbours(Set<Edge> connectingEdges) {
        // TODO: P1.4
       Set<Tile> connectedNeighbours = new HashSet<>();

       for (Edge edge : connectingEdges)
       {
           Set<TilePosition> adjacentTilePositions = edge.getAdjacentTilePositions();

           for (TilePosition adjacentTilePosition : adjacentTilePositions)
           {
               Tile neighbour = getHexGrid().getTileAt(adjacentTilePosition);

               if (neighbour!=null && neighbour.equals(this)==false)
               {
                   connectedNeighbours.add(neighbour);
               }
           }
       }
       return connectedNeighbours;
    }

    @Override
    public Set<Edge> getRails(Player player) {
        return getEdges().stream().filter(edge -> edge.getRailOwners().contains(player)).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "TileImpl[" +
                "position=" + position +
                ", type=" + type +
                "]";
    }
}
