package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.ChallengeScene;
import uk.ac.soton.comp1206.scene.GameScene;
import uk.ac.soton.comp1206.scene.InstructionsScene;
import uk.ac.soton.comp1206.scene.IntroScene;
import uk.ac.soton.comp1206.scene.LobbyScene;
import uk.ac.soton.comp1206.scene.MenuScene;
import uk.ac.soton.comp1206.scene.MultiplayerScene;
import uk.ac.soton.comp1206.scene.ScoreScene;

public class GameWindow {
    private static final Logger logger = LogManager.getLogger(GameWindow.class);
    private final int width;
    private final int height;
    private final Stage stage;
    private GameScene currentScene;
    private Scene scene;
    Communicator communicator = null;

    public GameWindow(Stage stage, int width, int height) {
        this.width = width;
        this.height = height;
        this.stage = stage;
        this.setupStage();
        this.setupResources();
        this.setupDefaultScene();
        this.communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");
        this.startIntro();
    }

    private void setupResources() {
        logger.info("Loading resources");
        Font.loadFont(this.getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"), 32.0D);
        Font.loadFont(this.getClass().getResourceAsStream("/style/Orbitron-Bold.ttf"), 32.0D);
        Font.loadFont(this.getClass().getResourceAsStream("/style/Orbitron-ExtraBold.ttf"), 32.0D);
    }

    public void setupDefaultScene() {
        Scene scene = new Scene(new Pane(), (double)this.width, (double)this.height, Color.BLACK);
        this.scene = scene;
        this.stage.setScene(this.scene);
    }

    public void setupStage() {
        this.stage.setTitle("TetrECS");
        this.stage.setMinWidth((double)this.width);
        this.stage.setMinHeight((double)(this.height + 20));
        this.stage.setOnCloseRequest((ev) -> {
            App.getInstance().shutdown();
        });
    }

    public void loadScene(GameScene newScene) {
        this.cleanup();
        newScene.build();
        this.currentScene = newScene;
        this.scene = newScene.setScene();
        this.stage.setScene(this.scene);
        Platform.runLater(() -> {
            this.currentScene.initialise();
        });
    }

    public void cleanup() {
        logger.info("Clearing up previous scene");
        this.communicator.clearListeners();
    }

    public void startMenu() {
        this.loadScene(new MenuScene(this));
    }

    public void startIntro() {
        this.loadScene(new IntroScene(this));
    }

    public void startChallenge() {
        this.loadScene(new ChallengeScene(this));
    }

    public void startMultiplayer() {
        this.loadScene(new MultiplayerScene(this));
    }

    public void startInstructions() {
        this.loadScene(new InstructionsScene(this));
    }

    public void startLobby() {
        this.loadScene(new LobbyScene(this));
    }

    public void startScores(Game game) {
        this.loadScene(new ScoreScene(this, game));
    }

    public Scene getScene() {
        return this.scene;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Communicator getCommunicator() {
        return this.communicator;
    }
}
