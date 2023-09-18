package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.Random;
import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

public class MultiplayerGame extends Game {
  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  private final Communicator communicator;
  private Random randomiser = new Random();
  private ArrayDeque<GamePiece> incoming = new ArrayDeque();

  public MultiplayerGame(Communicator communicator, int cols, int rows) {
    super(cols, rows);
    this.communicator = communicator;
    communicator.addListener((message) -> {
      Platform.runLater(() -> {
        this.receiveMessage(message.trim());
      });
    });
  }

  private void receiveMessage(String message) {
    logger.info("Received message: {}", message);
    String[] components = message.split(" ", 2);
    String command = components[0];
    String data;
    if (command.equals("PIECE") && components.length > 1) {
      data = components[1];
      this.receivePiece(Integer.parseInt(data));
    } else if (command.equals("SCORES") && components.length > 1) {
      data = components[1];
      this.receiveScores(data);
    }

  }

  private void receiveScores(String data) {
    logger.info("Received scores: {}", data);
    this.scores.clear();
    String[] scoreLines = data.split("\\R");
    String[] var3 = scoreLines;
    int var4 = scoreLines.length;

    for(int var5 = 0; var5 < var4; ++var5) {
      String scoreLine = var3[var5];
      String[] components = scoreLine.split(":");
      String player = components[0];
      int score = Integer.parseInt(components[1]);
      logger.info("Received score: {} = {}", player, score);
      this.scores.add(new Pair(player, score));
    }

    this.scores.sort((a, b) -> {
      return ((Integer)b.getValue()).compareTo((Integer)a.getValue());
    });
  }

  private void receivePiece(int block) {
    GamePiece piece = GamePiece.createPiece(block, this.randomiser.nextInt(3));
    logger.info("`Received next piece: {}", piece);
    this.incoming.add(piece);
    logger.info("(receivePiece) Queue: {}", this.incoming);
    if (!this.started && this.incoming.size() > 2) {
      logger.info("Initial pieces received, the game begins");
      this.nextPiece = this.spawnPiece();
      this.nextPiece();
      this.started = true;
    }

  }

  public boolean blockAction(GameBlock gameBlock) {
    boolean result = super.blockAction(gameBlock);
    this.communicator.send("BOARD " + this.encode());
    return result;
  }

  public String encode() {
    StringBuilder board = new StringBuilder();

    for(int x = 0; x < this.cols; ++x) {
      for(int y = 0; y < this.rows; ++y) {
        int var10001 = this.grid.get(x, y);
        board.append(var10001 + " ");
      }
    }

    return board.toString().trim();
  }

  public void initialise() {
    logger.info("Initialising game");
    this.score.set(0);
    this.level.set(0);
    this.lives.set(3);
    this.multiplier.set(1);
    this.initialPieces();
  }

  public void initialPieces() {
    for(int i = 0; i < 5; ++i) {
      this.communicator.send("PIECE");
    }

  }

  public GamePiece spawnPiece() {
    logger.info("(spawnPiece) Queue: {}", this.incoming);
    this.communicator.send("PIECE");
    return (GamePiece)this.incoming.pop();
  }
}
