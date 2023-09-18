package uk.ac.soton.comp1206.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedHandler;
import uk.ac.soton.comp1206.event.RightClickHandler;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {
    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;
    private boolean readOnly = false;
    private boolean showCentre = false;
    private BooleanProperty keyboardMode = new SimpleBooleanProperty(false);

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedHandler onBlockClicked;
    private RightClickHandler onRightClicked;
    private GameBlock hover;

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;
        this.setup();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols, rows);
        this.setup();
    }

    public void addPiece(GamePiece piece, int placeX, int placeY) {
        this.grid.addPiece(piece, placeX, placeY);
    }

    public void clear() {
        this.grid.clear();
    }

    public void setPiece(GamePiece piece) {
        this.clear();
        this.addPiece(piece);
    }

    public void addPiece(GamePiece piece) {
        this.addPiece(piece, 0, 0);
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return this.blocks[x][y];
    }

    public void setup() {
        this.getStyleClass().add("gameboard");
        this.setMaxWidth(this.width);
        this.setMaxHeight(this.height);
        this.setGridLinesVisible(true);
        this.blocks = new GameBlock[this.cols][this.rows];

        for(int y = 0; y < this.rows; ++y) {
            for(int x = 0; x < this.cols; ++x) {
                double cwidth = this.width / (double)this.cols;
                double cheight = this.height / (double)this.rows;
                GameBlock block = new GameBlock(this, x, y, cwidth, cheight);
                this.blocks[x][y] = block;
                this.add(block, x, y);
                block.bind(this.grid.getGridProperty(x, y));
                block.setOnMouseClicked((e) -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        this.click(block);
                    } else {
                        this.rightClick(block);
                    }

                });
                block.setOnMouseEntered((e) -> {
                    this.hover(block);
                });
                block.setOnMouseExited((e) -> {
                    this.unhover(block);
                });
            }
        }

    }

    public void click(GameBlock block) {
        if (this.onBlockClicked != null) {
            this.onBlockClicked.blockClicked(block);
        }

    }

    public void rightClick(GameBlock block) {
        if (this.onRightClicked != null) {
            this.onRightClicked.rightClicked();
        }

    }

    public void hover(GameBlock block) {
        if (!this.readOnly) {
            if (this.keyboardMode.get() && this.hover != null) {
                this.unhover(this.hover);
            }

            this.hover = block;
            block.setHovering(true);
        }
    }

    public void unhover(GameBlock block) {
        if (!this.readOnly) {
            block.setHovering(false);
        }
    }

    public void highlight(GameBlock block) {
        block.highlight();
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setOnClick(BlockClickedHandler handler) {
        this.onBlockClicked = handler;
    }

    public void setOnRighttClick(RightClickHandler handler) {
        this.onRightClicked = handler;
    }

    public void showCentre(boolean showCentre) {
        this.showCentre = showCentre;
        if (showCentre) {
            Double midX = Math.ceil((double)this.rows / 2.0D) - 1.0D;
            Double midY = Math.ceil((double)this.cols / 2.0D) - 1.0D;
            this.blocks[midX.intValue()][midY.intValue()].setCentre(true);
        }

    }

    public BooleanProperty keyboardModeProperty() {
        return this.keyboardMode;
    }
}
