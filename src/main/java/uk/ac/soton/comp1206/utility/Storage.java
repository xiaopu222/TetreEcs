package uk.ac.soton.comp1206.utility;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Storage {
  private static final Logger logger = LogManager.getLogger(Storage.class);

  public Storage() {
  }

  public static ArrayList<Pair<String, Integer>> loadScores() {
    logger.info("Loading scores from scores.txt");
    ArrayList result = new ArrayList();

    try {
      Path path = Paths.get("scores.txt");
      if (Files.notExists(path, new LinkOption[0])) {
        initialiseScores();
      }

      List<String> scores = Files.readAllLines(path);
      logger.info("Read {} scores", scores.stream().count());
      Iterator var3 = scores.iterator();

      while(var3.hasNext()) {
        String score = (String)var3.next();
        String[] components = score.split(":");
        result.add(new Pair(components[0], Integer.parseInt(components[1])));
      }
    } catch (Exception var6) {
      logger.error("Unable to read from scores.txt: " + var6.getMessage());
      var6.printStackTrace();
    }

    return result;
  }

  public static void initialiseScores() {
    logger.info("Initialising scores for the first time");
    ArrayList<Pair<String, Integer>> result = new ArrayList();

    for(int i = 0; i < 10; ++i) {
      result.add(new Pair("Oli", 1000 * (10 - i)));
    }

    writeScores(result);
  }

  public static void writeScores(List<Pair<String, Integer>> scores) {
    logger.info("Writing {} scores to scores.txt", scores.stream().count());
    scores.sort((a, b) -> {
      return ((Integer)b.getValue()).compareTo((Integer)a.getValue());
    });

    try {
      Path path = Paths.get("scores.txt");
      StringBuilder result = new StringBuilder();
      int counter = 0;
      Iterator var4 = scores.iterator();

      while(var4.hasNext()) {
        Pair<String, Integer> score = (Pair)var4.next();
        ++counter;
        String var10001 = (String)score.getKey();
        result.append(var10001 + ":" + score.getValue() + "\n");
        if (counter >= 10) {
          break;
        }
      }

      Files.writeString(path, result.toString());
    } catch (Exception var6) {
      logger.error("Unable to write to scores.txt: " + var6.getMessage());
      var6.printStackTrace();
    }

  }
}
