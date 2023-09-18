package uk.ac.soton.comp1206.game;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ChallengeGame extends Game {
  private static final Logger logger = LogManager.getLogger(ChallengeGame.class);
  private Random randomMiser = new Random();

  public ChallengeGame(int cols, int rows) {
    super(cols, rows);
  }

  public GamePiece spawnPiece() {
    GamePiece piece = GamePiece.createPiece(this.randomMiser.nextInt(15), this.randomMiser.nextInt(3));
    logger.info("Spawning next piece: {}", piece);
    return piece;
  }
}
