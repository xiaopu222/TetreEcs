package uk.ac.soton.comp1206.scene;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public abstract class GameScene {
  final GameWindow gameWindow;
  protected Game game;
  protected GamePane root;
  protected Scene scene;

  public GameScene(GameWindow gameWindow) {
    this.gameWindow = gameWindow;
  }

  void setScene(Scene scene) {
    this.scene = scene;
  }

  public Scene getScene() {
    return this.scene;
  }

  public void initialise() {
  }

  public abstract void build();

  public Scene setScene() {
    Scene previous = this.gameWindow.getScene();
    Scene scene = new Scene(this.root, previous.getWidth(), previous.getHeight(), Color.BLACK);
    scene.getStylesheets().add(Multimedia.getStyle("game.css"));
    this.setScene(scene);
    return scene;
  }
}
