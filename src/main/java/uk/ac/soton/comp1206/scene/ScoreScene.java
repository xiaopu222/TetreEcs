package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoreBox;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import uk.ac.soton.comp1206.utility.Storage;

public class ScoreScene extends GameScene {
  private static final Logger logger = LogManager.getLogger(ScoreScene.class);
  private Timer timer;
  private boolean newScore = false;
  private boolean newRemoteScore = false;
  private Pair<String, Integer> myScore;
  private BorderPane mainPane;
  private ScoreBox hiscoreBlock;
  private ScoreBox hiscoreBlock2;
  private ObservableList<Pair<String, Integer>> scoreList;
  private ObservableList<Pair<String, Integer>> remoteScoreList;
  private ArrayList<Pair<String, Integer>> remoteScores = new ArrayList();
  private StringProperty myName = new SimpleStringProperty("");
  private BooleanProperty showScores = new SimpleBooleanProperty(false);
  private boolean waitingForScores = true;
  private VBox scoreBox;
  private Text hiscoreText;
  private Communicator communicator;

  public ScoreScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.communicator = gameWindow.getCommunicator();
    this.game = game;
    logger.info("Creating Score Scene");
  }

  public void startTimer(int delay) {
    logger.info("Starting timer, delay {}", delay);
    if (this.timer != null) {
      this.timer.cancel();
      this.timer.purge();
    }

    TimerTask task = new TimerTask() {
      public void run() {
        Platform.runLater(() -> {
          ScoreScene.this.returnToMenu();
        });
      }
    };
    this.timer = new Timer();
    this.timer.schedule(task, (long)delay);
  }

  public void returnToMenu() {
    if (!this.newScore) {
      if (this.timer != null) {
        this.timer.cancel();
      }

      this.gameWindow.startMenu();
    }
  }

  public void initialise() {
    Multimedia.playAudio("explode.wav");
    Multimedia.startBackgroundMusic("end.wav", false);
    this.communicator.addListener((message) -> {
      Platform.runLater(() -> {
        this.receiveMessage(message.trim());
      });
    });
    if (!this.game.getScores().isEmpty()) {
      this.myName.set(this.game.nameProperty().getValue());
    }

    this.communicator.send("HISCORES");
  }

  public void checkForHiScore() {
    logger.info("Checking for high score");
    if (!this.game.getScores().isEmpty()) {
      logger.info("Online scores, not checking for high score");
      this.reveal();
    } else {
      logger.info("Checking for high score");
      int currentScore = this.game.getScore();
      int counter = 0;
      int remoteCounter = 0;
      int lowestScore = 0;
      if (this.scoreList.size() > 0) {
        lowestScore = (Integer)((Pair)this.scoreList.get(this.scoreList.size() - 1)).getValue();
      }

      if (this.scoreList.size() < 10) {
        this.newScore = true;
      }

      int lowestScoreRemote = 0;
      if (this.remoteScores.size() > 0) {
        lowestScoreRemote = (Integer)((Pair)this.remoteScores.get(this.remoteScores.size() - 1)).getValue();
      }

      if (this.remoteScores.size() < 10) {
        this.newRemoteScore = true;
      }

      Iterator var6;
      Pair score;
      if (currentScore > lowestScore) {
        for(var6 = this.scoreList.iterator(); var6.hasNext(); ++counter) {
          score = (Pair)var6.next();
          if ((Integer)score.getValue() < currentScore) {
            logger.info("New local high score at position {}", counter);
            this.newScore = true;
            break;
          }
        }
      }

      if (currentScore > lowestScoreRemote) {
        for(var6 = this.remoteScores.iterator(); var6.hasNext(); ++remoteCounter) {
          score = (Pair)var6.next();
          if ((Integer)score.getValue() < currentScore) {
            logger.info("New remote high score at position {}", remoteCounter);
            this.newRemoteScore = true;
            break;
          }
        }
      }

      logger.info("New score: {}, New remote score: {}", this.newScore, this.newRemoteScore);
      if (!this.newScore && !this.newRemoteScore) {
        logger.info("No high score");
        this.reveal();
      } else {
        this.hiscoreText.setText("You got a High Score!");
        TextField name = new TextField();
        name.setPromptText("Enter your name");
        name.setPrefWidth((double)(this.gameWindow.getWidth() / 2));
        name.requestFocus();
        this.scoreBox.getChildren().add(2, name);
        Button button = new Button("Submit");
        button.setDefaultButton(true);
        this.scoreBox.getChildren().add(3, button);
        int finalCounter = counter;
        int finalRemoteCounter = remoteCounter;
        button.setOnAction((e) -> {
          String myName = name.getText().replace(":", "");
          this.myName.set(myName);
          this.scoreBox.getChildren().remove(2);
          this.scoreBox.getChildren().remove(2);
          this.myScore = new Pair(myName, currentScore);
          if (this.newScore) {
            this.scoreList.add(finalCounter, this.myScore);
          }

          if (this.newRemoteScore) {
            this.remoteScoreList.add(finalRemoteCounter, this.myScore);
          }

          this.communicator.send("HISCORE " + myName + ":" + currentScore);
          Storage.writeScores(this.scoreList);
          this.communicator.send("HISCORES");
          this.newScore = false;
          this.newRemoteScore = false;
          Multimedia.playAudio("pling.wav");
        });
      }

    }
  }

  public void reveal() {
    this.startTimer(15000);
    this.scene.setOnKeyPressed((e) -> {
      this.returnToMenu();
    });
    this.showScores.set(true);
    this.hiscoreBlock.reveal();
    this.hiscoreBlock2.reveal();
  }

  private void receiveMessage(String message) {
    logger.info("Received message: {}", message);
    String[] components = message.split(" ", 2);
    String command = components[0];
    if (command.equals("HISCORES")) {
      if (components.length > 1) {
        String data = components[1];
        this.receiveScores(data);
      } else {
        this.receiveScores("");
      }
    }

  }

  private void receiveScores(String data) {
    logger.info("Received scores: {}", data);
    this.remoteScores.clear();
    String[] scoreLines = data.split("\\R");
    String[] var3 = scoreLines;
    int var4 = scoreLines.length;

    for(int var5 = 0; var5 < var4; ++var5) {
      String scoreLine = var3[var5];
      String[] components = scoreLine.split(":", 2);
      String player = components[0];
      int score = Integer.parseInt(components[1]);
      logger.info("Received score: {} = {}", player, score);
      this.remoteScores.add(new Pair(player, score));
    }

    this.remoteScores.sort((a, b) -> {
      return ((Integer)b.getValue()).compareTo((Integer)a.getValue());
    });
    this.remoteScoreList.clear();
    this.remoteScoreList.addAll(this.remoteScores);
    if (this.waitingForScores) {
      this.checkForHiScore();
      this.waitingForScores = false;
    } else {
      this.reveal();
    }
  }

  public void build() {
    logger.info("Building " + this.getClass().getName());
    this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
    StackPane scorePane = new StackPane();
    scorePane.setMaxWidth((double)this.gameWindow.getWidth());
    scorePane.setMaxHeight((double)this.gameWindow.getHeight());
    scorePane.getStyleClass().add("menu-background");
    this.root.getChildren().add(scorePane);
    this.mainPane = new BorderPane();
    scorePane.getChildren().add(this.mainPane);
    this.scoreBox = new VBox();
    this.scoreBox.setAlignment(Pos.TOP_CENTER);
    this.scoreBox.setPadding(new Insets(5.0D, 5.0D, 5.0D, 5.0D));
    this.scoreBox.setSpacing(20.0D);
    this.mainPane.setCenter(this.scoreBox);
    ImageView image = new ImageView(Multimedia.getImage("TetrECS.png"));
    image.setFitWidth((double)this.gameWindow.getWidth() * 0.6666666666666666D);
    image.setPreserveRatio(true);
    this.scoreBox.getChildren().add(image);
    Text gameOverText = new Text("Game Over");
    gameOverText.setTextAlignment(TextAlignment.CENTER);
    VBox.setVgrow(gameOverText, Priority.ALWAYS);
    gameOverText.getStyleClass().add("bigtitle");
    this.scoreBox.getChildren().add(gameOverText);
    this.hiscoreText = new Text("High Scores");
    this.hiscoreText.setTextAlignment(TextAlignment.CENTER);
    VBox.setVgrow(this.hiscoreText, Priority.ALWAYS);
    this.hiscoreText.getStyleClass().add("title");
    this.hiscoreText.setFill(Color.YELLOW);
    this.scoreBox.getChildren().add(this.hiscoreText);
    GridPane scoreGrid = new GridPane();
    scoreGrid.visibleProperty().bind(this.showScores);
    scoreGrid.setAlignment(Pos.CENTER);
    scoreGrid.setHgap(100.0D);
    this.scoreBox.getChildren().add(scoreGrid);
    Text localScoresLabel = new Text("Local Scores");
    localScoresLabel.setTextAlignment(TextAlignment.CENTER);
    localScoresLabel.getStyleClass().add("heading");
    GridPane.setHalignment(localScoresLabel, HPos.CENTER);
    scoreGrid.add(localScoresLabel, 0, 0);
    Text remoteScoresLabel = new Text("Online Scores");
    remoteScoresLabel.setTextAlignment(TextAlignment.CENTER);
    remoteScoresLabel.getStyleClass().add("heading");
    GridPane.setHalignment(remoteScoresLabel, HPos.CENTER);
    scoreGrid.add(remoteScoresLabel, 1, 0);
    this.hiscoreBlock = new ScoreBox();
    Button button = new Button("Button");
    this.hiscoreBlock.getChildren().add(button);
    GridPane.setHalignment(this.hiscoreBlock, HPos.CENTER);
    scoreGrid.add(this.hiscoreBlock, 0, 1);
    this.hiscoreBlock2 = new ScoreBox();
    Button button2 = new Button("Button");
    this.hiscoreBlock2.getChildren().add(button2);
    GridPane.setHalignment(this.hiscoreBlock2, HPos.CENTER);
    scoreGrid.add(this.hiscoreBlock2, 1, 1);
    if (this.game.getScores().isEmpty()) {
      this.scoreList = FXCollections.observableArrayList(Storage.loadScores());
    } else {
      this.scoreList = FXCollections.observableArrayList(this.game.getScores());
      localScoresLabel.setText("This game");
    }

    this.scoreList.sort((a, b) -> {
      return ((Integer)b.getValue()).compareTo((Integer)a.getValue());
    });
    this.remoteScoreList = FXCollections.observableArrayList(this.remoteScores);
    SimpleListProperty<Pair<String, Integer>> wrapper = new SimpleListProperty(this.scoreList);
    this.hiscoreBlock.scoreProperty().bind(wrapper);
    this.hiscoreBlock.nameProperty().bind(this.myName);
    SimpleListProperty<Pair<String, Integer>> wrapper2 = new SimpleListProperty(this.remoteScoreList);
    this.hiscoreBlock2.scoreProperty().bind(wrapper2);
    this.hiscoreBlock2.nameProperty().bind(this.myName);
  }
}
