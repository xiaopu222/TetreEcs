package uk.ac.soton.comp1206.scene;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class LobbyScene extends GameScene {
  private static final Logger logger = LogManager.getLogger(LobbyScene.class);
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
  private ScrollPane scroller = null;
  private VBox messages = null;
  private BorderPane mainPane;
  private Communicator communicator;
  private LobbyScene.ChannelList channelList;
  private Timer timer;
  private LobbyScene.GameBox gameBox;
  private LobbyScene.PlayerList playerList;
  private StringProperty channel = new SimpleStringProperty("");
  private BooleanProperty host = new SimpleBooleanProperty(false);
  private StringProperty name = new SimpleStringProperty();

  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    this.communicator = gameWindow.getCommunicator();
    logger.info("Creating Lobby Scene");
  }

  private void cleanup() {
    if (this.timer != null) {
      this.timer.purge();
      this.timer.cancel();
      this.timer = null;
    }

  }

  public void initialise() {
    this.scene.setOnKeyPressed((e) -> {
      if (e.getCode().equals(KeyCode.ESCAPE)) {
        this.leave();
        this.cleanup();
        this.gameWindow.startMenu();
      }

    });
    this.sendMessage("LIST");
    this.communicator.addListener((message) -> {
      Platform.runLater(() -> {
        this.receiveMessage(message.trim());
      });
    });
    TimerTask refreshChannels = new TimerTask() {
      public void run() {
        LobbyScene.logger.info("Refreshing channel list");
        LobbyScene.this.sendMessage("LIST");
      }
    };
    this.timer = new Timer();
    this.timer.schedule(refreshChannels, 0L, 5000L);
  }

  private void leave() {
    if (!this.channel.isEmpty().get()) {
      this.sendMessage("PART");
    }

  }

  private void receiveMessage(String message) {
    logger.info("Received message: {}", message);
    String[] components = message.split(" ", 2);
    String command = components[0];
    String error;
    if (command.equals("CHANNELS")) {
      if (components.length > 1) {
        error = components[1];
        this.receiveChannelList(error);
      } else {
        this.clearChannelList();
      }
    } else if (command.equals("START")) {
      logger.info("Received game start");
      this.startGame();
    } else if (command.equals("JOIN")) {
      this.host.set(false);
      error = components[1];
      logger.info("Joined {}", error);
      this.join(error);
    } else if (command.equals("PARTED")) {
      logger.info("Left channel");
      this.channel.set("");
    } else if (command.equals("HOST")) {
      logger.info("Promoted to host");
      this.host.set(true);
    } else if (command.equals("MSG")) {
      error = components[1];
      this.receiveMsg(error);
    } else if (command.equals("NICK") && components.length > 1) {
      error = components[1];
      if (!error.contains(":")) {
        this.setName(components[1]);
      }
    } else if (command.equals("USERS") && components.length > 1) {
      this.setUsers(components[1]);
    } else if (command.equals("ERROR")) {
      error = components[1];
      logger.error(error);
      Alert alert = new Alert(AlertType.ERROR, error, new ButtonType[0]);
      alert.showAndWait();
    }

  }

  private void setUsers(String data) {
    logger.info("Received channel user list: {}", data);
    String[] players = data.split("\\R");
    List<String> list = Arrays.asList(players);
    this.playerList.setPlayers(list);
    Multimedia.playAudio("message.wav");
  }

  private void setName(String name) {
    logger.info("My name is: " + name);
    this.name.set(name);
  }

  private void receiveMsg(String data) {
    String[] components = data.split(":", 2);
    TextFlow message = new TextFlow();
    message.getStyleClass().add("message");
    DateTimeFormatter var10002 = formatter;
    Text timestamp = new Text("[" + var10002.format(LocalDateTime.now()) + "] ");
    Text nick = new Text("<" + components[0] + "> ");
    Text msg = new Text(components[1]);
    message.getChildren().addAll(new Node[]{timestamp, nick, msg});
    this.messages.getChildren().add(message);
    Multimedia.playAudio("message.wav");
    this.scroller.getParent().layout();
    this.scroller.layout();
    this.scroller.setVvalue(1.0D);
  }

  private void clearChannelList() {
    this.channelList.setChannels(new ArrayList());
  }

  private void receiveChannelList(String data) {
    logger.info("Received channel list: {}", data);
    String[] channels = data.split("\\R");
    List<String> list = Arrays.asList(channels);
    this.channelList.setChannels(list);
  }

  private void sendMessage(String message) {
    logger.info("Sending message: {}", message);
    this.communicator.send(message);
  }

  private void sendMsg(String message) {
    if (message.startsWith("/")) {
      String[] components = message.split(" ", 2);
      String command = components[0].toLowerCase();
      if (command.equals("/nick") && components.length > 1) {
        this.sendMessage("NICK " + components[1]);
      } else if (command.equals("/start") && this.host.get()) {
        this.sendMessage("START");
      } else if (command.equals("/part")) {
        this.sendMessage("PART");
      }
    } else {
      this.sendMessage("MSG " + message);
    }

  }

  public void build() {
    logger.info("Building " + this.getClass().getName());
    this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
    StackPane lobbyPane = new StackPane();
    lobbyPane.setMaxWidth((double)this.gameWindow.getWidth());
    lobbyPane.setMaxHeight((double)this.gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    this.root.getChildren().add(lobbyPane);
    this.mainPane = new BorderPane();
    lobbyPane.getChildren().add(this.mainPane);
    Text multiplayerText = new Text("Multiplayer");
    BorderPane.setAlignment(multiplayerText, Pos.CENTER);
    multiplayerText.setTextAlignment(TextAlignment.CENTER);
    multiplayerText.getStyleClass().add("title");
    this.mainPane.setTop(multiplayerText);
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10.0D);
    gridPane.setVgap(10.0D);
    gridPane.setPadding(new Insets(5.0D, 5.0D, 5.0D, 5.0D));
    Text channelText = new Text("Current Games");
    channelText.setTextAlignment(TextAlignment.CENTER);
    channelText.getStyleClass().add("heading");
    gridPane.add(channelText, 0, 0);
    this.mainPane.setCenter(gridPane);
    this.channelList = new LobbyScene.ChannelList();
    this.channelList.channelProperty().bind(this.channel);
    gridPane.add(this.channelList, 0, 1);
    Text lobbyText = new Text();
    lobbyText.textProperty().bind(this.channel);
    lobbyText.setTextAlignment(TextAlignment.CENTER);
    lobbyText.getStyleClass().add("heading");
    gridPane.add(lobbyText, 1, 0);
    this.gameBox = new LobbyScene.GameBox();
    gridPane.add(this.gameBox, 1, 1);
    this.gameBox.visibleProperty().bind(this.channel.isNotEmpty());
    GridPane.setHgrow(this.gameBox, Priority.ALWAYS);
  }

  public void requestJoin(String channelName) {
    if (!((String)this.channel.get()).equals(channelName)) {
      this.host.set(false);
      this.sendMessage("JOIN " + channelName);
    }
  }

  public void join(String channelName) {
    this.channelList.addChannel(channelName);
    this.channel.set(channelName);
    this.messages.getChildren().clear();
    Text intro = new Text("Welcome to the lobby\nType /nick NewName to change your name\n\n");
    this.messages.getChildren().add(intro);
  }

  public void requestStart() {
    logger.info("Requesting game start");
    this.sendMessage("START");
  }

  public void startGame() {
    logger.info("Game is now starting!");
    this.cleanup();
    this.gameWindow.startMultiplayer();
  }

  public class PlayerList extends TextFlow {
    private ArrayList<String> players = new ArrayList();

    public PlayerList() {
      this.getStyleClass().add("playerBox");
    }

    public void setPlayers(List<String> newPlayers) {
      this.players.clear();
      this.players.addAll(newPlayers);
      this.update();
    }

    public void update() {
      this.getChildren().clear();

      Text playerName;
      for(Iterator var1 = this.players.iterator(); var1.hasNext(); this.getChildren().add(playerName)) {
        String player = (String)var1.next();
        playerName = new Text(player + " ");
        if (player.equals(LobbyScene.this.name.get())) {
          playerName.getStyleClass().add("myname");
        }
      }

    }
  }

  public class ChannelList extends VBox {
    private HashMap<String, Text> channelList = new HashMap();
    private StringProperty channel = new SimpleStringProperty();

    public ChannelList() {
      this.setSpacing(10.0D);
      this.setPadding(new Insets(5.0D, 5.0D, 5.0D, 5.0D));
      this.setPrefWidth(300.0D);
      this.getStyleClass().add("channelList");
      Text newChannel = new Text("Host New Game");
      newChannel.getStyleClass().add("channelItem");
      this.getChildren().add(newChannel);
      this.channel.addListener(new ChangeListener<String>() {
        public void changed(ObservableValue<? extends String> observable, String oldValue, String channelName) {
          Iterator var4 = ChannelList.this.channelList.values().iterator();

          while(var4.hasNext()) {
            Text all = (Text)var4.next();
            all.getStyleClass().remove("selected");
          }

          if (!channelName.isEmpty()) {
            Text channelText = (Text)ChannelList.this.channelList.get(channelName);
            channelText.getStyleClass().add("selected");
          }
        }
      });
      TextField newChannelField = new TextField();
      newChannelField.setVisible(false);
      this.getChildren().add(newChannelField);
      newChannel.setOnMouseClicked((e) -> {
        Multimedia.playAudio("rotate.wav");
        newChannelField.setVisible(true);
        newChannelField.requestFocus();
      });
      newChannelField.setOnKeyPressed((e) -> {
        if (e.getCode().equals(KeyCode.ENTER)) {
          Multimedia.playAudio("rotate.wav");
          LobbyScene.this.sendMessage("CREATE " + newChannelField.getText().trim());
          LobbyScene.this.sendMessage("LIST");
          newChannelField.setVisible(false);
          newChannelField.clear();
        }

      });
    }

    public StringProperty channelProperty() {
      return this.channel;
    }

    public void addChannel(String channelName) {
      if (!this.channelList.containsKey(channelName)) {
        LobbyScene.logger.info("Adding {}", channelName);
        Text channelText = new Text(channelName);
        channelText.getStyleClass().add("channelItem");
        this.getChildren().add(channelText);
        this.channelList.put(channelName, channelText);
        channelText.setOnMouseClicked((e) -> {
          LobbyScene.this.requestJoin(channelName);
        });
      }
    }

    public void setChannels(List<String> channels) {
      Set<String> existing = this.channelList.keySet();
      if (existing.size() != channels.size() || !existing.containsAll(channels)) {
        Set<String> toRemove = new HashSet();
        Iterator var4 = existing.iterator();

        String channelName;
        while(var4.hasNext()) {
          channelName = (String)var4.next();
          if (!channels.contains(channelName)) {
            toRemove.add(channelName);
          }
        }

        var4 = toRemove.iterator();

        while(var4.hasNext()) {
          channelName = (String)var4.next();
          this.getChildren().remove(this.channelList.get(channelName));
        }

        this.channelList.keySet().removeAll(toRemove);
        var4 = channels.iterator();

        while(var4.hasNext()) {
          channelName = (String)var4.next();
          this.addChannel(channelName);
        }

      }
    }
  }

  public class GameBox extends VBox {
    public GameBox() {
      this.setSpacing(10.0D);
      this.setPadding(new Insets(5.0D, 5.0D, 5.0D, 5.0D));
      this.getStyleClass().add("gameBox");
      LobbyScene.this.playerList = LobbyScene.this.new PlayerList();
      this.getChildren().add(LobbyScene.this.playerList);
      LobbyScene.this.scroller = new ScrollPane();
      LobbyScene.this.scroller.setPrefHeight((double)(LobbyScene.this.gameWindow.getHeight() / 2));
      LobbyScene.this.scroller.getStyleClass().add("scroller");
      LobbyScene.this.scroller.setFitToWidth(true);
      LobbyScene.this.messages = new VBox();
      LobbyScene.this.messages.getStyleClass().add("messages");
      this.getChildren().add(LobbyScene.this.scroller);
      LobbyScene.this.scroller.setContent(LobbyScene.this.messages);
      TextField sendMessage = new TextField();
      sendMessage.setPromptText("Send a message");
      sendMessage.getStyleClass().add("messageBox");
      sendMessage.setOnKeyPressed((e) -> {
        if (e.getCode().equals(KeyCode.ENTER)) {
          LobbyScene.this.sendMsg(sendMessage.getText());
          sendMessage.clear();
        }
      });
      this.getChildren().add(sendMessage);
      AnchorPane buttons = new AnchorPane();
      this.getChildren().add(buttons);
      Button partButton = new Button("Leave game");
      partButton.setOnAction((e) -> {
        LobbyScene.this.sendMessage("PART");
        LobbyScene.this.sendMessage("LIST");
      });
      buttons.getChildren().add(partButton);
      AnchorPane.setRightAnchor(partButton, 0.0D);
      Button startButton = new Button("Start game");
      startButton.visibleProperty().bind(LobbyScene.this.host);
      startButton.setOnAction((e) -> {
        LobbyScene.this.requestStart();
      });
      buttons.getChildren().add(startButton);
      AnchorPane.setLeftAnchor(startButton, 0.0D);
    }
  }
}
