package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.ScoreBox;
import uk.ac.soton.comp1206.component.TimeBar;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class MultiplayerScene extends ChallengeScene {
  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
  private final Communicator communicator;
  private ObservableList<Pair<String, Integer>> remoteScoreList;
  private ArrayList<Pair<String, Integer>> remoteScores = new ArrayList();
  private ScoreBox leaderboard;
  private StringProperty name = new SimpleStringProperty();
  private Text receivedMessage;
  private TextField sendMessage;

  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Starting multiplayer scene");
    this.communicator = gameWindow.getCommunicator();
  }

  public void initialise() {
    logger.info("Initialised {}", this.getClass());
    Multimedia.startBackgroundMusic("game_start.wav", "game.wav");
    this.game.scoreProperty().addListener(this::setScore);
    this.game.setOnLineCleared(this::lineCleared);
    this.game.setOnGameLoop(this::gameLoop);
    this.game.setOnNextPiece(this::nextPiece);
    this.scene.setOnKeyPressed(this::handleKey);
    this.startGame();
    this.communicator.addListener((message) -> {Platform.runLater(() -> {this.receiveMessage(message.trim());
      });
    });
    this.updateName();
    this.updateScores();
    this.game.livesProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        MultiplayerScene.this.sendMessage("LIVES " + newValue);
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
    this.game.scoreProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        MultiplayerScene.this.sendMessage("SCORE " + newValue);
      }
    });
    this.game.setOnGameOver(() -> {
      this.endGame();
      this.gameWindow.startScores(this.game);
    });
  }

  private void updateName() {
    this.sendMessage("NICK");
  }

  private void updateScores() {
    this.sendMessage("SCORES");
  }

  private void sendMessage(String message) {
    this.communicator.send(message);
  }

  private void receiveMessage(String message) {
    logger.info("Received message: {}", message);
    String[] components = message.split(" ", 2);
    String command = components[0];
    String data;
    if (command.equals("SCORES") && components.length > 1) {
      data = components[1];
      this.receiveScores(data);
    } else if (command.equals("NICK") && components.length > 1) {
      data = components[1];
      if (!data.contains(":")) {
        this.setName(components[1]);
      }
    } else if (command.equals("MSG")) {
      data = components[1];
      this.receiveMsg(data);
    }

  }

  private void receiveMsg(String data) {
    logger.info("Receieved chat: " + data);
    String[] components = data.split(":", 2);
    String username = components[0];
    if (username.equals(this.name.get())) {
      this.chatMode = false;
    }

    String msg = components[1];
    this.receivedMessage.setText("<" + username + " > " + msg);
    Multimedia.playAudio("message.wav");
  }

  private void setName(String name) {
    logger.info("My name is: " + name);
    this.name.set(name);
    this.game.nameProperty().set(name);
  }

  private void receiveScores(String data) {
    logger.info("Received scores: {}", data);
    this.remoteScores.clear();
    String[] scoreLines = data.split("\\R");
    String[] var3 = scoreLines;
    int var4 = scoreLines.length;

    for(int var5 = 0; var5 < var4; ++var5) {
      String scoreLine = var3[var5];
      String[] components = scoreLine.split(":");
      String player = components[0];
      int score = Integer.parseInt(components[1]);
      logger.info("Received score: {} = {}", player, score);
      String lives = components[2];
      if (lives.equals("DEAD")) {
        this.leaderboard.kill(player);
      }

      this.remoteScores.add(new Pair(player, score));
    }

    this.remoteScores.sort((a, b) -> {
      return ((Integer)b.getValue()).compareTo((Integer)a.getValue());
    });
    this.remoteScoreList.clear();
    this.remoteScoreList.addAll(this.remoteScores);
  }

  public void endGame() {
    super.endGame();
    this.sendMessage("DIE");
  }

  public void setupGame() {
    logger.info("Starting a new multiplayer game");
    this.game = new MultiplayerGame(this.communicator, 5, 5);
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
    this.root.getChildren().add(challengePane);
    BorderPane layout = new BorderPane();
    challengePane.getChildren().add(layout);
    VBox mainBoard = new VBox();
    mainBoard.setAlignment(Pos.CENTER);
    BorderPane.setAlignment(mainBoard, Pos.CENTER);
    layout.setCenter(mainBoard);
    this.board = new GameBoard(this.game.getGrid(), (double)(this.gameWindow.getWidth() / 2), (double)(this.gameWindow.getWidth() / 2));
    this.board.keyboardModeProperty().bind(this.keyboardMode);
    mainBoard.getChildren().add(this.board);
    VBox.setVgrow(mainBoard, Priority.ALWAYS);
    this.receivedMessage = new Text("In-Game Chat: Press T to send a chat message");
    TextFlow receivedMessageFlow = new TextFlow();
    receivedMessageFlow.setTextAlignment(TextAlignment.CENTER);
    receivedMessageFlow.getChildren().add(this.receivedMessage);
    receivedMessageFlow.getStyleClass().add("messages");
    mainBoard.getChildren().add(receivedMessageFlow);
    this.sendMessage = new TextField();
    this.sendMessage.setVisible(false);
    this.sendMessage.setEditable(false);
    this.sendMessage.getStyleClass().add("messageBox");
    this.sendMessage.setOnKeyPressed((e) -> {
      if (e.getCode().equals(KeyCode.ESCAPE)) {
        this.sendMsg("");
      }

      if (e.getCode().equals(KeyCode.ENTER)) {
        this.sendMsg(this.sendMessage.getText());
        this.sendMessage.clear();
      }
    });
    mainBoard.getChildren().add(this.sendMessage);
    VBox sideBar = new VBox();
    sideBar.setAlignment(Pos.TOP_CENTER);
    sideBar.setSpacing(6.0D);
    sideBar.setPadding(new Insets(5.0D, 5.0D, 5.0D, 5.0D));
    layout.setRight(sideBar);
    GridPane topBar = new GridPane();
    topBar.setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));
    layout.setTop(topBar);
    VBox scoreBox = new VBox();
    scoreBox.setAlignment(Pos.CENTER);
    Text scoreLabel = new Text("Score");
    scoreLabel.textProperty().bind(this.name);
    scoreLabel.getStyleClass().add("heading");
    scoreBox.getChildren().add(scoreLabel);
    Text scoreField = new Text("0");
    scoreField.getStyleClass().add("score");
    scoreField.textProperty().bind(this.score.asString());
    scoreBox.getChildren().add(scoreField);
    topBar.add(scoreBox, 0, 0);
    Text title = new Text("Multiplayer Match");
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
    Text hiscoreLabel = new Text("Versus");
    hiscoreLabel.getStyleClass().add("heading");
    sideBar.getChildren().add(hiscoreLabel);
    this.remoteScoreList = FXCollections.observableArrayList(this.remoteScores);
    SimpleListProperty<Pair<String, Integer>> scoreWrapper = new SimpleListProperty(this.remoteScoreList);
    this.leaderboard = new ScoreBox();
    this.leaderboard.getStyleClass().add("leaderboard");
    this.leaderboard.setAutoReveal(true);
    this.leaderboard.setScoresToShow(5);
    this.leaderboard.scoreProperty().bind(scoreWrapper);
    this.leaderboard.nameProperty().bind(this.name);
    sideBar.getChildren().add(this.leaderboard);
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

  protected void startChat() {
    this.chatMode = true;
    Platform.runLater(() -> {
      this.sendMessage.setVisible(true);
      this.sendMessage.setEditable(true);
      this.sendMessage.requestFocus();
    });
  }

  private void sendMsg(String message) {
    this.sendMessage.setEditable(false);
    this.sendMessage.setVisible(false);
    this.sendMessage("MSG " + message);
  }
}
