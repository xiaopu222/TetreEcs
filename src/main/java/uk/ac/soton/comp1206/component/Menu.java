package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;

public class Menu extends Group {
  //Vbox contains the selection of this game
  private VBox box = new VBox(5.0D);
  //the arrayList of menu item
  private ArrayList<MenuItem> items = new ArrayList();
  private int selected = -1;

  public Menu(int width, int height) {
    this.box.setAlignment(Pos.CENTER);
    this.box.getStyleClass().add("menu");
    this.setOnMouseMoved((e) -> {
      Iterator var2 = this.items.iterator();

      while(var2.hasNext()) {
        MenuItem item = (MenuItem)var2.next();
        item.deselect();
      }

    });
    this.getChildren().add(this.box);
  }

  private void paint() {
    Iterator var1 = this.items.iterator();

    while(var1.hasNext()) {
      MenuItem item = (MenuItem)var1.next();
      item.deselect();
    }

    ((MenuItem)this.items.get(this.selected)).select();
  }

  public void add(String label, Runnable action) {
    MenuItem item = new MenuItem(label);
    this.items.add(item);
    this.box.getChildren().add(item);
    item.setOnAction(action);
  }

  public void up() {
    if (this.selected > 0) {
      --this.selected;
    } else if (this.selected < 0) {
      this.selected = 0;
    }

    this.paint();
  }

  public void down() {
    if (this.selected < this.items.size() - 1) {
      ++this.selected;
    }

    this.paint();
  }

  public void select() {
    ((MenuItem)this.items.get(this.selected)).fire();
  }
}
