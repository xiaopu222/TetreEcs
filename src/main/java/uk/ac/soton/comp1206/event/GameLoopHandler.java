package uk.ac.soton.comp1206.event;



public interface GameLoopHandler {
  /**
   *  once the lives are decreased, the Game start another loop
   * @param var1
   */
  void gameLoop(int var1);
}
