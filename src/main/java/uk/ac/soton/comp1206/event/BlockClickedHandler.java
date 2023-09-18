package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Block Clicked listener is used to handle the event when a block in a GameBoard is clicked. It passes the
 * GameBlock that was clicked in the message
 */
public interface BlockClickedHandler {

  /**
   * Handle a block clicked event
   * @param var1 the block that was clicked
   */
  void blockClicked(GameBlock var1);
}
