package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {
    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    public static final Color[] COLOURS;
    private final GameBoard gameBoard;
    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private boolean centre = false;
    private boolean hovering;
    private GameBlock.HighlightTimer timer;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        this.setWidth(width);
        this.setHeight(height);

        //Do an initial paint
        this.paint();

        //When the value property is updated, call the internal updateValue method
        this.value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        this.paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        if (this.timer != null) {
            this.timer.stop();
            this.timer = null;
        }

        if (this.value.get() == 0) {
            this.paintEmpty();
        } else {
            this.paintColor(COLOURS[this.value.get()]);
        }

        if (this.centre) {
            this.paintCentre();
        }

        if (this.hovering) {
            this.paintHover();
        }

    }

    public void paintCentre() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.color(1.0D, 1.0D, 1.0D, 0.5D));
        gc.fillOval(this.width / 4.0D, this.height / 4.0D, this.width / 2.0D, this.height / 2.0D);
    }

    /**
     * Paint this canvas empty
     */
    public void paintEmpty() {
        GraphicsContext gc = this.getGraphicsContext2D();

        //Clear
        gc.clearRect(0.0D, 0.0D, this.width, this.height);
        Color start = Color.color(0.0D, 0.0D, 0.0D, 0.3D);
        Color end = Color.color(0.0D, 0.0D, 0.0D, 0.7D);

        //Fill
        gc.setFill(new LinearGradient(0.0D, 0.0D, 1.0D, 1.0D, true, CycleMethod.REFLECT, new Stop[]{new Stop(0.0D, start), new Stop(1.0D, end)}));
        gc.fillRect(0.0D, 0.0D, this.width, this.height);
        gc.setStroke(Color.color(1.0D, 1.0D, 1.0D, 0.5D));
        gc.strokeRect(0.0D, 0.0D, this.width, this.height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    public void paintColor(Paint colour) {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0.0D, 0.0D, this.width, this.height);
        gc.setFill(colour);
        gc.fillRect(0.0D, 0.0D, this.width, this.height);
        gc.setFill(Color.color(1.0D, 1.0D, 1.0D, 0.12D));
        gc.fillPolygon(new double[]{0.0D, 0.0D, this.width}, new double[]{0.0D, this.height, this.height}, 3);
        gc.setFill(Color.color(1.0D, 1.0D, 1.0D, 0.3D));
        gc.fillRect(0.0D, 0.0D, this.width, 3.0D);
        gc.setFill(Color.color(1.0D, 1.0D, 1.0D, 0.3D));
        gc.fillRect(0.0D, 0.0D, 3.0D, this.height);
        gc.setFill(Color.color(0.0D, 0.0D, 0.0D, 0.3D));
        gc.fillRect(this.width - 3.0D, 0.0D, this.width, this.height);
        gc.setFill(Color.color(0.0D, 0.0D, 0.0D, 0.3D));
        gc.fillRect(0.0D, this.height - 3.0D, this.width, this.height);
        gc.setStroke(Color.color(0.0D, 0.0D, 0.0D, 0.5D));
        gc.strokeRect(0.0D, 0.0D, this.width, this.height);
    }

    public void paintHover() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.color(1.0D, 1.0D, 1.0D, 0.5D));
        gc.fillRect(0.0D, 0.0D, this.width, this.height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return this.y;
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        this.value.bind(input);
    }


    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * set  centre
     * @param centre
     */
    public void setCentre(boolean centre) {
        this.centre = true;
        this.paint();
    }

    public void setHovering(boolean hovering) {
        this.hovering = hovering;
        this.paint();
    }

    public void highlight() {
        this.timer = new GameBlock.HighlightTimer();
        this.timer.start();
    }

    static {
        /**
         * The set of colours for different pieces
         */
        COLOURS = new Color[]{Color.TRANSPARENT, Color.DEEPPINK, Color.RED, Color.ORANGE, Color.YELLOW, Color.YELLOWGREEN, Color.LIME, Color.GREEN, Color.DARKGREEN, Color.DARKTURQUOISE, Color.DEEPSKYBLUE, Color.AQUA, Color.AQUAMARINE, Color.BLUE, Color.MEDIUMPURPLE, Color.PURPLE};
    }


    /**
     * flash to show the block is gone
     */
    public class HighlightTimer extends AnimationTimer {
        double opacity = 1.0D;

        public HighlightTimer() {
        }

        public void handle(long now) {
            this.fadeOut();
        }

        /**
         * fadeOut special effects
         */
        private void fadeOut() {
            //painting block empty
            GameBlock.this.paintEmpty();
            this.opacity -= 0.02D;
            if (this.opacity <= 0.0D) {
                this.stop();
                GameBlock.this.timer = null;
            } else {
                GraphicsContext gc = GameBlock.this.getGraphicsContext2D();
                gc.setFill(Color.color(0.0D, 1.0D, 0.0D, this.opacity));
                gc.fillRect(0.0D, 0.0D, GameBlock.this.width, GameBlock.this.height);
            }
        }
    }
}
