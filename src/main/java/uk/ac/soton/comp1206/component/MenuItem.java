package uk.ac.soton.comp1206.component;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.utility.Multimedia;

public class MenuItem extends Group {
  private Text text;
  private Rectangle selection;
  private Runnable action;

  public MenuItem(String name) {
    this.text = new Text(name);
    this.text.getStyleClass().add("menuItem");
    this.getChildren().add(this.text);
  }

  public void select() {
    this.text.getStyleClass().add("selected");
  }

  public void deselect() {
    this.text.getStyleClass().remove("selected");
  }

  public void setOnAction(Runnable action) {
    this.action = action;
    this.setOnMouseClicked((e) -> {
      Multimedia.playAudio("rotate.wav");
      action.run();
    });
  }

  public void fire() {
    this.action.run();
  }
}
