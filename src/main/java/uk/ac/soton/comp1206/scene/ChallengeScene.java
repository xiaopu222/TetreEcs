package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.TimeBar;
import uk.ac.soton.comp1206.game.ChallengeGame;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import uk.ac.soton.comp1206.utility.Storage;

public class ChallengeScene extends GameScene {
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected IntegerProperty score = new SimpleIntegerProperty(0);
    protected IntegerProperty hiscore = new SimpleIntegerProperty(0);
    protected BooleanProperty keyboardMode = new SimpleBooleanProperty(false);
    protected int keyboardX = 0;
    protected int keyboardY = 0;
    protected TimeBar timer;
    protected StackPane timerStack;
    protected GameBoard board;
    protected GameBoard nextPiece;
    protected GameBoard nextPiece2;
    protected boolean chatMode = false;

    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    public void setupGame() {
        logger.info("Starting a new challenge");
        this.game = new ChallengeGame(5, 5);
    }

    public void initialise() {
        logger.info("Initialised {}", this.getClass());
        Multimedia.startBackgroundMusic("Open_It_Up.mp3", "Open_It_Up.mp3");
        this.game.scoreProperty().addListener(this::setScore);
        this.game.setOnLineCleared(this::lineCleared);
        this.game.setOnGameLoop(this::gameLoop);
        this.game.setOnNextPiece(this::nextPiece);
        this.scene.setOnKeyPressed(this::handleKey);
        ArrayList<Pair<String, Integer>> scores = Storage.loadScores();
        this.hiscore.set((Integer)((Pair)scores.get(0)).getValue());
        this.startGame();
        this.game.livesProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue.intValue() > newValue.intValue()) {
                    Multimedia.playAudio("lifelose.wav");
                } else {
                    Multimedia.playAudio("lifegain.wav");
                }

            }
        });
        this.game.levelProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    Multimedia.playAudio("level.wav");
                }

            }
        });
        this.game.setOnGameOver(() -> {
            this.endGame();
            this.gameWindow.startScores(this.game);
        });
    }

    public void startGame() {
        logger.info("Starting game");
        this.game.start();
    }

    public void endGame() {
        logger.info("Ending game");
        this.game.stop();
        Multimedia.stopAll();
    }

    public void build() {
        logger.info("Building " + this.getClass().getName());
        this.setupGame();

        this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
        StackPane challengePane = new StackPane();
        challengePane.setBackground(new Background(new BackgroundFill[]{new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)}));
        challengePane.setMaxWidth((double)this.gameWindow.getWidth());
        challengePane.setMaxHeight((double)this.gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");

        //add challengePane to root
        this.root.getChildren().add(challengePane);
        //create a borderPane called layout
        BorderPane layout = new BorderPane();
        //layout is added to challengePane
        challengePane.getChildren().add(layout);
        this.board = new GameBoard(this.game.getGrid(), (double)(this.gameWindow.getWidth() / 2), (double)(this.gameWindow.getWidth() / 2));
        this.board.keyboardModeProperty().bind(this.keyboardMode);

        //The game board
        layout.setCenter(this.board);

        //A VBox is sed to show the details about high score,level,incoming piece
        VBox sideBar = new VBox();
        sideBar.setAlignment(Pos.CENTER);
        sideBar.setSpacing(6.0D);
        sideBar.setPadding(new Insets(5.0D, 5.0D, 5.0D, 5.0D));
        layout.setRight(sideBar);
        GridPane topBar = new GridPane();
        topBar.setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));
        layout.setTop(topBar);

        VBox scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        Text scoreLabel = new Text("Score");
        scoreLabel.getStyleClass().add("heading");
        scoreBox.getChildren().add(scoreLabel);
        Text scoreField = new Text("0");
        scoreField.getStyleClass().add("score");
        scoreField.textProperty().bind(this.score.asString());
        scoreBox.getChildren().add(scoreField);
        topBar.add(scoreBox, 0, 0);
        Text title = new Text("Challenge Mode");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.getStyleClass().add("title");
        title.setTextAlignment(TextAlignment.CENTER);
        topBar.add(title, 1, 0);
        GridPane.setFillWidth(title, true);
        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHalignment(title, HPos.CENTER);
        VBox liveBox = new VBox();
        liveBox.setAlignment(Pos.CENTER);
        Text livesLabel = new Text("Lives");
        livesLabel.getStyleClass().add("heading");
        liveBox.getChildren().add(livesLabel);
        Text livesField = new Text("0");
        livesField.getStyleClass().add("lives");
        livesField.textProperty().bind(this.game.livesProperty().asString());
        liveBox.getChildren().add(livesField);
        topBar.add(liveBox, 2, 0);
        Text hiscoreLabel = new Text("High Score");
        hiscoreLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(hiscoreLabel);
        Text hiscoreField = new Text("0");
        hiscoreField.getStyleClass().add("hiscore");
        sideBar.getChildren().add(hiscoreField);
        hiscoreField.textProperty().bind(this.hiscore.asString());
        Text levelLabel = new Text("Level");
        levelLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(levelLabel);
        Text levelField = new Text("0");
        levelField.getStyleClass().add("level");
        sideBar.getChildren().add(levelField);
        levelField.textProperty().bind(this.game.levelProperty().asString());
        Text nextPieceLabel = new Text("Incoming");
        nextPieceLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(nextPieceLabel);
        this.nextPiece = new GameBoard(3, 3, (double)(this.gameWindow.getWidth() / 6), (double)(this.gameWindow.getWidth() / 6));
        this.nextPiece.setReadOnly(true);
        this.nextPiece.showCentre(true);
        this.nextPiece.setOnClick(this::rotateBlock);
        sideBar.getChildren().add(this.nextPiece);
        this.nextPiece2 = new GameBoard(3, 3, (double)(this.gameWindow.getWidth() / 10), (double)(this.gameWindow.getWidth() / 10));
        this.nextPiece2.setReadOnly(true);
        this.nextPiece2.setPadding(new Insets(20.0D, 0.0D, 0.0D, 0.0D));
        this.nextPiece2.setOnClick(this::swapBlock);
        sideBar.getChildren().add(this.nextPiece2);
        this.board.setOnRighttClick(this::rotateBlock);
        this.board.setOnClick(this::blockClicked);
        this.timerStack = new StackPane();
        layout.setBottom(this.timerStack);
        this.timer = new TimeBar();
        BorderPane.setMargin(this.timerStack, new Insets(5.0D, 5.0D, 5.0D, 5.0D));
        this.timerStack.getChildren().add(this.timer);
        StackPane.setAlignment(this.timer, Pos.CENTER_LEFT);
    }

    protected void nextPiece(GamePiece nextPiece) {
        logger.info("Next piece to place: " + nextPiece);
        this.nextPiece.setPiece(nextPiece);
        this.nextPiece2.setPiece(this.game.getNextPiece());
    }

    protected void gameLoop(int nextLoop) {
        logger.info("Game Loop");
        Timeline timeline = new Timeline(new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(this.timer.fillProperty(), Color.GREEN)}), new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(this.timer.widthProperty(), this.timerStack.getWidth())}), new KeyFrame(new Duration((double)nextLoop * 0.5D), new KeyValue[]{new KeyValue(this.timer.fillProperty(), Color.YELLOW)}), new KeyFrame(new Duration((double)nextLoop * 0.75D), new KeyValue[]{new KeyValue(this.timer.fillProperty(), Color.RED)}), new KeyFrame(new Duration((double)nextLoop), new KeyValue[]{new KeyValue(this.timer.widthProperty(), 0)})});
        timeline.play();
    }

    protected void lineCleared(Collection<GameBlockCoordinate> blocks) {
        Iterator var2 = blocks.iterator();

        while(var2.hasNext()) {
            GameBlockCoordinate block = (GameBlockCoordinate)var2.next();
            this.board.highlight(this.board.getBlock(block.getX(), block.getY()));
        }

        Multimedia.playAudio("clear.wav");
    }

    protected void swapBlock(GameBlock gameBlock) {
        this.swapBlock();
    }

    protected void swapBlock() {
        logger.info("Swapping block");
        Multimedia.playAudio("rotate.wav");
        this.game.swapCurrentPiece();
        this.nextPiece.setPiece(this.game.getCurrentPiece());
        this.nextPiece2.setPiece(this.game.getNextPiece());
    }

    protected void rotateBlock(GameBlock gameBlock) {
        this.rotateBlock();
    }

    protected void rotateBlock() {
        this.rotateBlock(1);
    }

    protected void rotateBlock(int rotations) {
        logger.info("Rotating block");
        Multimedia.playAudio("rotate.wav");
        this.game.rotateCurrentPiece(rotations);
        this.nextPiece.setPiece(this.game.getCurrentPiece());
    }

    protected void handleKey(KeyEvent keyEvent) {
        if (!this.chatMode) {
            this.keyboardMode.set(true);
            if (!keyEvent.getCode().equals(KeyCode.ENTER) && !keyEvent.getCode().equals(KeyCode.X)) {
                if (!keyEvent.getCode().equals(KeyCode.SPACE) && !keyEvent.getCode().equals(KeyCode.R)) {
                    if (!keyEvent.getCode().equals(KeyCode.A) && !keyEvent.getCode().equals(KeyCode.LEFT)) {
                        if (!keyEvent.getCode().equals(KeyCode.D) && !keyEvent.getCode().equals(KeyCode.RIGHT)) {
                            if (!keyEvent.getCode().equals(KeyCode.W) && !keyEvent.getCode().equals(KeyCode.UP)) {
                                if (!keyEvent.getCode().equals(KeyCode.S) && !keyEvent.getCode().equals(KeyCode.DOWN)) {
                                    if (!keyEvent.getCode().equals(KeyCode.Q) && !keyEvent.getCode().equals(KeyCode.Z) && !keyEvent.getCode().equals(KeyCode.OPEN_BRACKET)) {
                                        if (!keyEvent.getCode().equals(KeyCode.E) && !keyEvent.getCode().equals(KeyCode.C) && !keyEvent.getCode().equals(KeyCode.CLOSE_BRACKET)) {
                                            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                                                this.endGame();
                                                this.gameWindow.startMenu();
                                            } else if (keyEvent.getCode().equals(KeyCode.T)) {
                                                this.startChat();
                                            }
                                        } else {
                                            this.rotateBlock();
                                        }
                                    } else {
                                        this.rotateBlock(3);
                                    }
                                } else if (this.keyboardY < this.game.getRows() - 1) {
                                    ++this.keyboardY;
                                }
                            } else if (this.keyboardY > 0) {
                                --this.keyboardY;
                            }
                        } else if (this.keyboardX < this.game.getCols() - 1) {
                            ++this.keyboardX;
                        }
                    } else if (this.keyboardX > 0) {
                        --this.keyboardX;
                    }
                } else {
                    this.swapBlock();
                }
            } else {
                this.blockClicked(this.board.getBlock(this.keyboardX, this.keyboardY));
            }

            this.board.hover(this.board.getBlock(this.keyboardX, this.keyboardY));
        }
    }

    protected void startChat() {
    }

    protected void blockClicked(GameBlock gameBlock) {
        this.keyboardMode.set(false);
        this.blockAction(gameBlock);
    }

    protected void blockAction(GameBlock gameBlock) {
        if (this.game.blockAction(gameBlock)) {
            logger.info("Placed {}", gameBlock);
            Multimedia.playAudio("place.wav");
            this.game.restartGameLoop();
        } else {
            logger.info("Unable to place {}", gameBlock);
            Multimedia.playAudio("fail.wav");
        }

    }

    protected void setScore(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        logger.info("Score is now {}", newValue);
        if (newValue.intValue() > this.hiscore.get()) {
            this.hiscore.set(newValue.intValue());
        }

        Timeline timeline = new Timeline(new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(this.score, oldValue)}), new KeyFrame(new Duration(500.0D), new KeyValue[]{new KeyValue(this.score, newValue)})});
        timeline.play();
    }
}
