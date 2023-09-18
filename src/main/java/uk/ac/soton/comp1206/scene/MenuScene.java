package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.Menu;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class MenuScene extends GameScene {
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private BorderPane mainPane;
    private Menu gameMenu;

    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    public void initialise() {
        //My favorite rap song
        Multimedia.startBackgroundMusic("Open_It_Up.mp3");
        this.scene.setOnKeyPressed(this::handleKey);
    }

    private void handleKey(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            App.getInstance().shutdown();
        } else if (!keyEvent.getCode().equals(KeyCode.UP) && !keyEvent.getCode().equals(KeyCode.W)) {
            if (!keyEvent.getCode().equals(KeyCode.DOWN) && !keyEvent.getCode().equals(KeyCode.S)) {
                if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.SPACE)) {
                    this.gameMenu.select();
                }
            } else {
                this.gameMenu.down();
            }
        } else {
            this.gameMenu.up();
        }

    }

    public void build() {
        logger.info("Building " + this.getClass().getName());
        this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
        StackPane menuPane = new StackPane();
        menuPane.setMaxWidth((double)this.gameWindow.getWidth());
        menuPane.setMaxHeight((double)this.gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        this.root.getChildren().add(menuPane);
        this.mainPane = new BorderPane();
        menuPane.getChildren().add(this.mainPane);
        ImageView image = new ImageView(Multimedia.getImage("TetrECS.png"));
        image.setFitWidth((double)this.gameWindow.getHeight());
        image.setPreserveRatio(true);
        this.mainPane.setCenter(image);
        RotateTransition rotater = new RotateTransition(new Duration(3000.0D), image);
        rotater.setCycleCount(-1);
        rotater.setFromAngle(-5.0D);
        rotater.setToAngle(5.0D);
        rotater.setAutoReverse(true);
        rotater.play();
        this.gameMenu = new Menu(250, 150);
        BorderPane.setAlignment(this.gameMenu, Pos.CENTER);
        this.gameMenu.add("Single Player", () -> {
            this.gameWindow.startChallenge();
        });
        this.gameMenu.add("Multi Player", () -> {
            this.gameWindow.startLobby();
        });
        this.gameMenu.add("How to Play", () -> {
            this.gameWindow.startInstructions();
        });
        this.gameMenu.add("Exit", () -> {
            App.getInstance().shutdown();
        });
        this.mainPane.setBottom(this.gameMenu);
    }
}
