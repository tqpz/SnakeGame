import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameClient extends Application {
    private static boolean closed = false;
    //TODO sorting scoreboard by score -> on top best scores
    //TODO user want to set nickname once, not every time he restarts game
    ObservableList<String> test = FXCollections.observableArrayList();
    private String nick;
    private GameScene gameScene;

    public static void main(String args[]) throws Exception {
        launch(args);
    }

    public String parseNick(String nickWithScore) {
        String[] parts = nickWithScore.split(" ");
        String part1 = parts[0];
        String part2 = parts[1];
        int counter = 1;
        String dots = "";
        String showable = dots + part2;
        System.out.println(part1);

        while (showable.length() < 13) {
            dots += ".";
            showable = part1 + dots;
        }
        if (Integer.parseInt(part2) > 9) {
            dots = dots.substring(0, dots.length() - 1);
            showable = part1 + dots + part2;
        } else if (Integer.parseInt(part2) > 99) {
            dots = dots.substring(0, dots.length() - 2);
            showable = part1 + dots + part2;
        } else {
            showable = part1 + dots + part2;
        }
        return showable;
    }

    private void showNickPopup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hello!");
        dialog.setHeaderText("Register");
        dialog.setContentText("Please enter your name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            nick = name.replaceAll("\\s+", "").toUpperCase();
        });
    }

    public void parseInputString(String fromServer, ObservableList<String> items) {
        int lastcoma = 1;
        for (int i = 1; i < fromServer.length(); i++) {
            if (fromServer.charAt(i) == ',') {
                if (lastcoma > 2) {
                    items.add(fromServer.substring(lastcoma + 2, i));
                    lastcoma = i;
                } else {
                    items.add(fromServer.substring(lastcoma, i));
                    lastcoma = i;
                }
            }
        }
        sortScoreBoard(items);
    }

    public void sortScoreBoard(ObservableList<String> items) {

        for (int i = 0; i < items.size(); i++) {
            String[] parts = items.get(i).split(" ");
            items.remove(i);
            String part1 = parts[0]; // 004
            String part2 = parts[1];
            items.add(i, part2 + " " + part1);
        }

        Collections.sort(items, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });

        for (int i = 0; i < items.size(); i++) {
            String[] parts = items.get(i).split(" ");
            items.remove(i);
            String part1 = parts[0];
            String part2 = parts[1];
            int counter = 1;
            String dots = "";
            String showable = dots + part2;
            System.out.println(part2);

            while (showable.length() < 13) {
                dots += ".";
                showable = part2 + dots;
            }
            if (Integer.parseInt(part1) > 9) {
                dots = dots.substring(0, dots.length() - 1);
                showable = part2 + dots + part1;
            } else if (Integer.parseInt(part1) > 99) {
                dots = dots.substring(0, dots.length() - 2);
                showable = part2 + dots + part1;
            } else {
                showable = part2 + dots + part1;
            }
            items.add(i, showable);
        }

        Collections.reverse(items);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        test.add("leraw 43");
        test.add("rawkr 22");
        test.add("rawra 61");
        test.add("adawd 5");
        test.add("wdawd 1");
        test.add("wassd 9");

        sortScoreBoard(test);

        primaryStage.setTitle("Snake");
        primaryStage.setResizable(false);

        BorderPane root = new BorderPane();
        gameScene = new GameScene();
        Pane settingBar = new Pane();
        root.setPadding(new Insets(10, 20, 10, 20));

        Button reset = new Button("Try again");
        Button start = new Button("Start");

        ListView<String> players = new ListView<>();
        players.getStyleClass().add("scoreboard-font");

        ObservableList<String> items = FXCollections.observableArrayList();
        players.setItems(items);
        players.setLayoutY(50);
        players.setMaxHeight(400);
        players.setMaxWidth(150);
        players.setFocusTraversable(false);

        Scene scene = new Scene(root, 1000, 620);
        reset.setLayoutX(50);

        settingBar.getChildren().addAll(reset, start, players);

        start.setOnAction(event -> {
            reset.setDisable(true);
            start.setDisable(true);

            String fromServer;
            items.clear();

            try {
                Socket clientSocket = new Socket("localhost", 4444);
                if (nick == null)
                    showNickPopup();
                gameScene.getTimer().start();

                primaryStage.setOnCloseRequest(e -> {
                    if (gameScene.isDead()) {
                        Platform.exit();
                        System.exit(0);
                        closed = true;
                    } else {
                        e.consume();
                    }
                });

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

                fromServer = inFromServer.readLine();
                System.out.println(fromServer);
                output.println(nick);

                fromServer = inFromServer.readLine();

                parseInputString(fromServer, items);

                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                //Background work
                                while (!gameScene.isDead()) {

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                try {
                                    output.println(gameScene.getLastScore());
                                    KeyFrame update = new KeyFrame(Duration.ONE, event -> {
                                        reset.setDisable(false);
                                        start.setDisable(true);
                                        String localNick = nick + " " + gameScene.getLastScore();
                                        localNick = parseNick(localNick);
                                        items.add(localNick);
                                    });

                                    Timeline tl = new Timeline(update);
                                    tl.setCycleCount(1);
                                    tl.play();
                                } catch (Exception e) {
                                    System.out.println("error");
                                }
                                final CountDownLatch latch = new CountDownLatch(1);
                                latch.await();
                                //Keep with the background work
                                return null;
                            }
                        };
                    }
                };
                service.start();
            } catch (Exception e) {
            }
        });

        reset.setOnAction(event -> {
            start.setDisable(false);
            gameScene.resetAll();
        });

        root.setCenter(gameScene);
        root.setRight(settingBar);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.DOWN && gameScene.getLastDirection() != KeyCode.UP)
                gameScene.setLastDirection(KeyCode.DOWN);
            else if (key.getCode() == KeyCode.UP && gameScene.getLastDirection() != KeyCode.DOWN)
                gameScene.setLastDirection(KeyCode.UP);
            else if (key.getCode() == KeyCode.LEFT && gameScene.getLastDirection() != KeyCode.RIGHT)
                gameScene.setLastDirection(KeyCode.LEFT);
            else if (key.getCode() == KeyCode.RIGHT && gameScene.getLastDirection() != KeyCode.LEFT)
                gameScene.setLastDirection(KeyCode.RIGHT);
        });
        root.getStylesheets().addAll("style.css");

        primaryStage.setScene(scene);
        primaryStage.show();

        //parseInputString(inFromServer.readLine(),items);

    }
}
