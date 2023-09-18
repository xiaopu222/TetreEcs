package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

public interface LineClearedHandler {
  /**
   * handler to deal with the clear lines
   * @param var1
   */
  void lineCleared(HashSet<GameBlockCoordinate> var1);
}
