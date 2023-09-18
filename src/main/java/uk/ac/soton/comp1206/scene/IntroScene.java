package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Menu;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class IntroScene extends GameScene {
  private static final Logger logger = LogManager.getLogger(IntroScene.class);
  private ImageView imageView;
  private ImageView ecsLogo;
  private MediaPlayer player;
  private Menu gameMenu;
  private SequentialTransition sequence;

  public IntroScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Intro Scene");
    Multimedia.playAudio("intro.mp3");
  }

  public void initialise() {
    this.scene.setOnKeyPressed((e) -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        Multimedia.stopAll();
        this.sequence.stop();
        this.gameWindow.startMenu();
      }

    });
  }

  public void build() {
    logger.info("Building " + this.getClass().getName());
    this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
    StackPane introPane = new StackPane();
    introPane.setMaxWidth((double)this.gameWindow.getWidth());
    introPane.setMaxHeight((double)this.gameWindow.getHeight());
    introPane.getStyleClass().add("intro");

    ImageView logo = new ImageView(Multimedia.getImage("ECSGames.png"));
    logo.setFitWidth((double)(this.gameWindow.getWidth() / 3));
    logo.setPreserveRatio(true);
    logo.setOpacity(0.0D);
    introPane.getChildren().add(logo);
    this.root.getChildren().add(introPane);
    FadeTransition fadeIn = new FadeTransition(new Duration(2000.0D), logo);
    fadeIn.setToValue(1.0D);
    PauseTransition pause = new PauseTransition(new Duration(1500.0D));
    FadeTransition fadeOut = new FadeTransition(new Duration(500.0D), logo);
    fadeOut.setToValue(0.0D);
    this.sequence = new SequentialTransition(new Animation[]{fadeIn, pause, fadeOut});
    this.sequence.play();
    this.sequence.setOnFinished((e) -> {
      this.gameWindow.startMenu();
    });
  }
}
