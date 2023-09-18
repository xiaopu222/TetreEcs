package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopHandler;
import uk.ac.soton.comp1206.event.GameOverHandler;
import uk.ac.soton.comp1206.event.LineClearedHandler;
import uk.ac.soton.comp1206.event.NextPieceHandler;

public abstract class Game {
    private static final Logger logger = LogManager.getLogger(Game.class);
    protected final ScheduledExecutorService executor;
    protected ScheduledFuture<?> nextLoop;
    protected boolean started = false;

    /**
     * Number of rows
     */
    protected int rows;

    /**
     * Number of columns
     */
    protected int cols;

    /**
     * The grid model linked to the game
     */
    protected Grid grid;
    protected IntegerProperty score = new SimpleIntegerProperty(0);
    protected IntegerProperty level = new SimpleIntegerProperty(0);
    protected IntegerProperty lives = new SimpleIntegerProperty(0);
    protected IntegerProperty multiplier = new SimpleIntegerProperty(0);
    protected StringProperty name = new SimpleStringProperty();
    protected ArrayList<Pair<String, Integer>> scores = new ArrayList();
    protected GamePiece currentPiece;
    protected GamePiece nextPiece;
    protected LineClearedHandler lineClearedHandler = null;
    protected GameLoopHandler gameLoopHandler = null;
    protected NextPieceHandler nextPieceHandler = null;
    private GameOverHandler gameOverHandler = null;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.grid = new Grid(cols, rows);
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public ArrayList<Pair<String, Integer>> getScores() {
        return this.scores;
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game!");
        this.initialise();
        this.startGameLoop();
    }


    /**
     * stop the game
     */
    public void stop() {
        logger.info("Aborting game!");
        this.executor.shutdownNow();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialise() {
        logger.info("Initialising game");
        this.score.set(0);
        this.level.set(0);
        this.lives.set(3);
        this.multiplier.set(1);
        this.nextPiece = this.spawnPiece();
        this.nextPiece();
        this.started = true;
    }

    /**
     * swap to next piece
     * @return
     */
    public GamePiece nextPiece() {
        this.currentPiece = this.nextPiece;
        this.nextPiece = this.spawnPiece();
        logger.info("Current piece is now: {}", this.currentPiece);
        logger.info("Next piece spawned: {}", this.nextPiece);
        if (this.nextPieceHandler != null) {
            this.nextPieceHandler.nextPiece(this.currentPiece);
        }

        return this.currentPiece;
    }


    public abstract GamePiece spawnPiece();


    public boolean blockAction(GameBlock gameBlock) {
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("Block clicked: {},{}", x, y);
        if (this.currentPiece == null) {
            logger.error("No current piece");
            return false;
        } else {
            boolean added = this.grid.addPieceCentered(this.currentPiece, gameBlock.getX(), gameBlock.getY());
            if (!added) {
                return false;
            } else {
                this.afterBlock();
                this.nextPiece();
                return true;
            }
        }
    }

    /**
     * clear the rows and cols
     */
    public void afterBlock() {
        int lines = 0;

        //HashSet to record the lines should be clear
        HashSet<IntegerProperty> clear = new HashSet();
        HashSet<GameBlockCoordinate> clearBlocks = new HashSet();

        int y;
        int total;
        int x;
        //for vertical lines
        for(y = 0; y < this.cols; ++y) {
            total = this.rows;

            for(x = 0; x < this.rows && this.grid.get(y, x) != 0; ++x) {
                --total;
            }

            if (total == 0) {
                ++lines;

                for(x = 0; x < this.rows; ++x) {
                    clear.add(this.grid.getGridProperty(y, x));
                    clearBlocks.add(new GameBlockCoordinate(y, x));
                }
            }
        }

        //for horizontal lines
        for(y = 0; y < this.rows; ++y) {
            total = this.rows;

            for(x = 0; x < this.cols && this.grid.get(x, y) != 0; ++x) {
                --total;
            }

            if (total == 0) {
                ++lines;

                for(x = 0; x < this.cols; ++x) {
                    clear.add(this.grid.getGridProperty(x, y));
                    clearBlocks.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        //
        if (lines == 0) {
            if (this.multiplier.get() > 1) {
                logger.info("Multiplier reset to 1");
                this.multiplier.set(1);
            }

        } else {
            logger.info("Cleared {} lines", lines);
            this.increaseScore(lines * clear.size() * 10 * this.multiplier.get());
            this.multiplier.set(this.multiplier.add(1).get());
            logger.info("Multiplier now at {}", this.multiplier.get());
            this.level.set(Math.floorDiv(this.score.get(), 1000));
            Iterator var7 = clear.iterator();

            while(var7.hasNext()) {
                IntegerProperty square = (IntegerProperty)var7.next();
                square.set(0);
            }

            if (this.lineClearedHandler != null) {
                this.lineClearedHandler.lineCleared(clearBlocks);
            }

        }
    }

    /**
     * method to calculated scores
     * @param amount
     */
    public void increaseScore(int amount) {
        this.score.set(this.score.add(amount).get());
    }

    /**
     * set GameLoop
     * @param handler
     */
    public void setOnGameLoop(GameLoopHandler handler) {
        this.gameLoopHandler = handler;
    }

    /**
     *
     * @param handler
     */
    public void setOnLineCleared(LineClearedHandler handler) {
        this.lineClearedHandler = handler;
    }

    public void setOnNextPiece(NextPieceHandler handler) {
        this.nextPieceHandler = handler;
    }

    public void setOnGameOver(GameOverHandler handler) {
        this.gameOverHandler = handler;
    }

    /**
     * IntegerProperty which recure the score
     * @return
     */
    public IntegerProperty scoreProperty() {
        return this.score;
    }

    /**
     * get score method
     * @return
     */
    public int getScore() {
        return this.scoreProperty().get();
    }

    /**
     *
     * @return lives
     */
    public IntegerProperty livesProperty() {
        return this.lives;
    }

    /**
     *
     * @return levels
     */
    public IntegerProperty levelProperty() {
        return this.level;
    }

    /**
     *
     * @return multipliers
     */
    public IntegerProperty multiplierProperty() {
        return this.multiplier;
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    public Grid getGrid() {
        return this.grid;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return this.rows;
    }

    public void startGameLoop() {
        this.nextLoop = this.executor.schedule(this::gameLoop, (long)this.getTimerDelay(), TimeUnit.MILLISECONDS);
        if (this.gameLoopHandler != null) {
            this.gameLoopHandler.gameLoop(this.getTimerDelay());
        }

    }

    /**
     *
     */
    public void restartGameLoop() {
        this.nextLoop.cancel(false);
        this.startGameLoop();
    }


    /**
     * the game is over when the player lost all the lives
     */
    public void gameOver() {
        logger.info("Game over!");
        if (this.gameOverHandler != null) {
            Platform.runLater(() -> {
                this.gameOverHandler.gameOver();
            });
        }

    }

    /**
     *  once the player lost one live, then start a new gameLoop
     */
    public void gameLoop() {
        logger.info("Executing Game Loop");
        if (this.multiplier.get() > 1) {
            logger.info("Multiplier reset to 1");
            this.multiplier.set(1);
        }

        this.decreaseLives();
        this.nextPiece();
        int nextRun = this.getTimerDelay();

        if (this.gameLoopHandler != null) {
            this.gameLoopHandler.gameLoop(nextRun);
        }

        this.nextLoop = this.executor.schedule(this::gameLoop, (long)nextRun, TimeUnit.MILLISECONDS);
    }

    /**
     * calculate the time which the player must place piece
     * @return
     */
    public int getTimerDelay() {
        return Math.max(2500, 12000 - 500 * this.level.get());
    }

    /**
     * method to return the next piece
     * @return
     */
    public GamePiece getNextPiece() {
        return this.nextPiece;
    }

    /**
     * method to return the current piece
     * @return
     */
    public GamePiece getCurrentPiece() {
        return this.currentPiece;
    }

    public void rotateCurrentPiece() {
        this.currentPiece.rotate();
    }

    public void rotateCurrentPiece(int rotations) {
        this.currentPiece.rotate(rotations);
    }

    /**
     * method to increase level
     */
    public void increaseLevel() {
        //level add 1 when it was called
        this.level.set(this.level.add(1).get());
    }

    /**
     * method to decrease lives
     */
    public void decreaseLives() {
        if (this.lives.get() > 0) {
            //print out the current live with message
            logger.info("Lost a life, new life count: {}", this.lives.get());
            this.lives.set(this.lives.subtract(1).get());
        } else {
            //if lives equals to 0, the game is over
            this.gameOver();
        }

    }

    public void swapCurrentPiece() {
        GamePiece holdingPiece = this.currentPiece;
        this.currentPiece = this.nextPiece;
        this.nextPiece = holdingPiece;
    }
}
