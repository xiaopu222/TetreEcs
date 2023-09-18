package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceHandler {
  /**
   * handler to swap to next piece
   * @param var1
   */
  void nextPiece(GamePiece var1);
}
