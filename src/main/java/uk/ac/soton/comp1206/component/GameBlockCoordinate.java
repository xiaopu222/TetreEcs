package uk.ac.soton.comp1206.component;

import javafx.beans.NamedArg;

/**
 * Represents a row and column representation of a block in the grid. Holds the x (column) and y (row).
 *
 * Useful for use in a set or list or other form of collection.
 */

public class GameBlockCoordinate {

    /**
     * Represents the column
     */
    private final int x;

    /**
     * Represents the row
     */
    private final int y;

    /**
     * A hash is computed to enable comparisons between this and other GameBlockCoordinates.
     */
    private int hash = 0;

    /**
     * Create a new GameBlockCoordinate which stores a row and column reference to a block
     * @param x column
     * @param y row
     */
    public GameBlockCoordinate(@NamedArg("x") int x, @NamedArg("y") int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Return the column (x)
     * @return column number
     */
    public int getX() {
        return this.x;
    }


    /**
     * Return the row (y)
     * @return the row number
     */
    public int getY() {
        return this.y;
    }

    /**
     * Add a row and column reference to this one and return a new GameBlockCoordinate
     * @param x additional columns
     * @param y additional rows
     * @return a new GameBlockCoordinate with the result of the addition
     */
    public GameBlockCoordinate add(int x, int y) {
        return new GameBlockCoordinate(this.getX() + x, this.getY() + y);
    }

    /**
     * Add another GameBlockCoordinate to this one, returning a new GameBlockCoordinate
     * @param point point to add
     * @return a new GameBlockCoordinate with the result of the addition
     */
    public GameBlockCoordinate add(GameBlockCoordinate point) {
        return this.add(point.getX(), point.getY());
    }

    /** Subtract a row and column reference to this one and return a new GameBlockCoordinate
     * @param x columns to remove
     * @param y rows to remove
     * @return a new GameBlockCoordinate with the result of the subtraction
     */
    public GameBlockCoordinate subtract(int x, int y) {
        return new GameBlockCoordinate(this.getX() - x, this.getY() - y);
    }

    /**
     * Subtract another GameBlockCoordinate to this one, returning a new GameBlockCoordinate
     * @param point point to subtract
     * @return a new GameBlockCoordinate with the result of the subtraction
     */
    public GameBlockCoordinate subtract(GameBlockCoordinate point) {
        return this.subtract(point.getX(), point.getY());
    }

    /**
     * Compare this GameBlockCoordinate to another GameBlockCoordinate
     * @param obj other object to compare to
     * @return true if equal, otherwise false
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof GameBlockCoordinate)) {
            return false;
        } else {
            GameBlockCoordinate other = (GameBlockCoordinate)obj;
            return this.getX() == other.getX() && this.getY() == other.getY();
        }
    }

    /**
     * Calculate a hash code of this GameBlockCoordinate, used for comparisons
     * @return hash code
     */
    public int hashCode() {
        if (this.hash == 0) {
            long bits = 7L;
            bits = 31L * bits + Double.doubleToLongBits((double)this.getX());
            bits = 31L * bits + Double.doubleToLongBits((double)this.getY());
            this.hash = (int)(bits ^ bits >> 32);
        }

        return this.hash;
    }

    /**
     * Return a string representation of this GameBlockCoordinate
     * @return string representation
     */
    public String toString() {
        int var10000 = this.getX();
        return "GameBlockCoordinate [x = " + var10000 + ", y = " + this.getY() + "]";
    }
}
