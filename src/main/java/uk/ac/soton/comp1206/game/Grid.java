package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        this.grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(int y = 0; y < rows; ++y) {
            for(int x = 0; x < cols; ++x) {
                this.grid[x][y] = new SimpleIntegerProperty(0);
            }
        }

    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return this.grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        this.grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            return this.grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException var4) {
            return -1;
        }
    }

    public void clear() {
        for(int y = 0; y < this.rows; ++y) {
            for(int x = 0; x < this.cols; ++x) {
                this.grid[x][y].set(0);
            }
        }

    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return this.cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return this.rows;
    }

    //method to judge whether a block can play a piece
    public boolean canPlace(GamePiece piece, int placeX, int placeY) {
        int[][] blocks = piece.getBlocks();

        //through all the x and y
        for(int x = 0; x < blocks.length; ++x) {
            for(int y = 0; y < blocks[x].length; ++y) {
                //value shows the blocks statement
                int value = blocks[x][y];
                if (value != 0) {
                    int gridValue = this.get(x + placeX, y + placeY);
                    //if gridValue not equal to 0, that means there already have a block
                    if (gridValue != 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //The method to add a piece into the game board
    public boolean addPiece(GamePiece piece, int placeX, int placeY) {
        int[][] blocks = piece.getBlocks();
        //firstly judge can this place play a piece
        if (!this.canPlace(piece, placeX, placeY)) {
            return false;
        } else {
            //through all cols and rows
            for(int x = 0; x < blocks.length; ++x) {
                for(int y = 0; y < blocks[x].length; ++y) {
                    int value = blocks[x][y];
                    if (value != 0) {
                        this.set(x + placeX, y + placeY, value);
                    }
                }
            }

            return true;
        }
    }

    public boolean addPieceCentered(GamePiece piece, int placeX, int placeY) {
        --placeX;
        --placeY;
        return this.addPiece(piece, placeX, placeY);
    }
}
