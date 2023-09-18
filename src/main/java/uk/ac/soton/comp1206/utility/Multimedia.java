package uk.ac.soton.comp1206.utility;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {
  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  private static boolean audioEnabled = true;
  private static MediaPlayer mediaPlayer;
  private static MediaPlayer backgroundPlayer;
  private static double backgroundVolume = 0.5D;

  public Multimedia() {
  }

  public static void changeMusic(String music) {
    changeMusic(music, false);
  }

  public static void stopAll() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
    }

    if (backgroundPlayer != null) {
      backgroundPlayer.stop();
    }

  }

  public static void changeMusic(String music, boolean resume) {
    if (audioEnabled) {
      logger.info("Changing music to: " + music);

      try {
        String toPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();
        Media play = new Media(toPlay);
        Duration previous = null;
        if (backgroundPlayer != null) {
          previous = backgroundPlayer.getCurrentTime();
          backgroundPlayer.stop();
        } else {
          resume = false;
        }

        backgroundPlayer = new MediaPlayer(play);
        backgroundPlayer.setVolume(backgroundVolume);
        if (resume) {
          backgroundPlayer.setStartTime(previous);
        }

        backgroundPlayer.play();
        backgroundPlayer.setOnEndOfMedia(() -> {
          startBackgroundMusic(music);
        });
      } catch (Exception var5) {
        audioEnabled = false;
        var5.printStackTrace();
        logger.error("Unable to play audio file, disabling audio");
      }

    }
  }

  public static void startBackgroundMusic(String musicIntro, String music) {
    if (audioEnabled) {
      logger.info("Starting background music: " + musicIntro + ", " + music);
      if (backgroundPlayer != null) {
        backgroundPlayer.stop();
      }

      try {
        String toPlay = Multimedia.class.getResource("/music/" + musicIntro).toExternalForm();
        Media play = new Media(toPlay);
        backgroundPlayer = new MediaPlayer(play);
        backgroundPlayer.setVolume(backgroundVolume);
        backgroundPlayer.setOnEndOfMedia(() -> {
          startBackgroundMusic(music);
        });
        backgroundPlayer.play();
      } catch (Exception var4) {
        audioEnabled = false;
        var4.printStackTrace();
        logger.error("Unable to play audio file, disabling audio");
      }

    }
  }

  public static void startBackgroundMusic(String music) {
    startBackgroundMusic(music, true);
  }

  public static void startBackgroundMusic(String music, boolean loop) {
    if (audioEnabled) {
      logger.info("Starting background music: " + music);
      if (backgroundPlayer != null) {
        backgroundPlayer.stop();
      }

      try {
        String toPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();
        Media play = new Media(toPlay);
        backgroundPlayer = new MediaPlayer(play);
        backgroundPlayer.setVolume(backgroundVolume);
        if (loop) {
          MediaPlayer var10001 = backgroundPlayer;
          backgroundPlayer.setCycleCount(-1);
        }

        backgroundPlayer.play();
      } catch (Exception var4) {
        audioEnabled = false;
        var4.printStackTrace();
        logger.error("Unable to play audio file, disabling audio");
      }

    }
  }

  public static void playAudio(String file) {
    if (audioEnabled) {
      String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
      logger.info("Playing audio: " + toPlay);

      try {
        Media play = new Media(toPlay);
        mediaPlayer = new MediaPlayer(play);
        mediaPlayer.play();
      } catch (Exception var3) {
        audioEnabled = false;
        var3.printStackTrace();
        logger.error("Unable to play audio file, disabling audio");
      }

    }
  }

  public static Image getImage(String image) {
    try {
      Image result = new Image(Multimedia.class.getResource("/images/" + image).toExternalForm());
      return result;
    } catch (Exception var2) {
      var2.printStackTrace();
      logger.error("Unable to load image: {}", image);
      return null;
    }
  }

  public static String getStyle(String stylesheet) {
    String css = Multimedia.class.getResource("/style/" + stylesheet).toExternalForm();
    return css;
  }
}
