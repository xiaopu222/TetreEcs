package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends GameScene {
  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);
  private BorderPane mainPane;

  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  public void initialise() {
    this.scene.setOnKeyPressed((e) -> {
      this.gameWindow.startMenu();
    });
  }

  public void build() {
    logger.info("Building " + this.getClass().getName());
    this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
    StackPane instructionsPane = new StackPane();
    instructionsPane.setMaxWidth((double)this.gameWindow.getWidth());
    instructionsPane.setMaxHeight((double)this.gameWindow.getHeight());
    instructionsPane.getStyleClass().add("menu-background");
    this.root.getChildren().add(instructionsPane);
    this.mainPane = new BorderPane();
    instructionsPane.getChildren().add(this.mainPane);
    VBox vBox = new VBox();
    BorderPane.setAlignment(vBox, Pos.CENTER);
    vBox.setAlignment(Pos.TOP_CENTER);
    this.mainPane.setCenter(vBox);
    Text instructions = new Text("Instructions");
    instructions.getStyleClass().add("heading");
    vBox.getChildren().add(instructions);
    Text instructionText = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
    TextFlow instructionFlow = new TextFlow(new Node[]{instructionText});
    instructionText.getStyleClass().add("instructions");
    instructionText.setTextAlignment(TextAlignment.CENTER);
    instructionFlow.setTextAlignment(TextAlignment.CENTER);
    vBox.getChildren().add(instructionFlow);
    ImageView instructionImage = new ImageView(this.getClass().getResource("/images/Instructions.png").toExternalForm());
    instructionImage.setFitWidth((double)this.gameWindow.getWidth() / 1.5D);
    instructionImage.setPreserveRatio(true);
    vBox.getChildren().add(instructionImage);
    Text pieces = new Text("Game Pieces");
    pieces.getStyleClass().add("heading");
    vBox.getChildren().add(pieces);
    GridPane gridPane = new GridPane();
    vBox.getChildren().add(gridPane);
    double padding = (double)(this.gameWindow.getWidth() - this.gameWindow.getWidth() / 14 * 5 - 50) / 2.0D;
    gridPane.setPadding(new Insets(0.0D, padding, 0.0D, padding));
    gridPane.setVgap(10.0D);
    gridPane.setHgap(10.0D);
    int x = 0;
    int y = 0;

    for(int i = 0; i < 15; ++i) {
      GamePiece piece = GamePiece.createPiece(i);
      GameBoard gameBoard = new GameBoard(3, 3, (double)(this.gameWindow.getWidth() / 14), (double)(this.gameWindow.getWidth() / 14));
      gameBoard.setPiece(piece);
      gameBoard.setReadOnly(true);
      gridPane.add(gameBoard, x, y);
      ++x;
      if (x == 5) {
        x = 0;
        ++y;
      }
    }

  }
}
