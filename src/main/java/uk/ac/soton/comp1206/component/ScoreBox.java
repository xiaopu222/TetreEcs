package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreBox extends VBox {
  private static final Logger logger = LogManager.getLogger(ScoreBox.class);
  public final SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty();
  private ArrayList<HBox> scoreBoxes = new ArrayList();
  private int scoresToShow = 10;
  private boolean autoReveal = false;
  private StringProperty name = new SimpleStringProperty();
  private ArrayList<String> deadPlayers = new ArrayList();

  public ScoreBox() {
    this.getStyleClass().add("scorelist");
    this.setAlignment(Pos.CENTER);
    this.setSpacing(2.0D);
    this.scores.addListener((InvalidationListener) (c) -> {
      this.updateList();
    });
    this.name.addListener((e) -> {
      this.updateList();
    });
  }

  public void setAutoReveal(boolean autoReveal) {
    this.autoReveal = autoReveal;
  }

  public void setScoresToShow(int amount) {
    this.scoresToShow = amount;
  }

  public void reveal() {
    logger.info("Revealing {} scores", this.scoreBoxes.size());
    ArrayList<Transition> transitions = new ArrayList();
    Iterator var2 = this.scoreBoxes.iterator();

    while(var2.hasNext()) {
      HBox scoreBox = (HBox)var2.next();
      FadeTransition fader = new FadeTransition(new Duration(300.0D), scoreBox);
      fader.setFromValue(0.0D);
      fader.setToValue(1.0D);
      transitions.add(fader);
    }

    SequentialTransition transition = new SequentialTransition((Animation[])transitions.toArray((x$0) -> {
      return new Animation[x$0];
    }));
    transition.play();
  }

  public void updateList() {
    logger.info("Updating score list with {} scores", this.scores.size());
    this.scoreBoxes.clear();
    this.getChildren().clear();
    int counter = 0;
    Iterator var2 = this.scores.iterator();

    while(var2.hasNext()) {
      Pair<String, Integer> score = (Pair)var2.next();
      ++counter;
      if (counter > this.scoresToShow) {
        break;
      }

      HBox scoreBox = new HBox();
      scoreBox.setOpacity(0.0D);
      scoreBox.getStyleClass().add("scoreitem");
      scoreBox.setAlignment(Pos.CENTER);
      scoreBox.setSpacing(10.0D);
      Color colour = GameBlock.COLOURS[counter];
      Text player = new Text((String)score.getKey() + ":");
      player.getStyleClass().add("scorer");
      if (((String)score.getKey()).equals(this.name.get())) {
        player.getStyleClass().add("myscore");
      }

      if (this.deadPlayers.contains(score.getKey())) {
        player.getStyleClass().add("deadscore");
      }

      player.setTextAlignment(TextAlignment.CENTER);
      player.setFill(colour);
      HBox.setHgrow(player, Priority.ALWAYS);
      Text points = new Text(((Integer)score.getValue()).toString());
      points.getStyleClass().add("points");
      points.setTextAlignment(TextAlignment.CENTER);
      points.setFill(colour);
      HBox.setHgrow(points, Priority.ALWAYS);
      scoreBox.getChildren().addAll(new Node[]{player, points});
      this.getChildren().add(scoreBox);
      this.scoreBoxes.add(scoreBox);
    }

    if (this.autoReveal) {
      this.reveal();
    }

  }

  public ListProperty<Pair<String, Integer>> scoreProperty() {
    return this.scores;
  }

  public StringProperty nameProperty() {
    return this.name;
  }

  public void kill(String player) {
    this.deadPlayers.add(player);
  }
}
