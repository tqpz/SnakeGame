import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mateusz on 29.03.2017.
 */

/*
Class responsible for showing window and ui
 */
public class GameClient extends Application {
    private String nick;
    private String ip;
    private GameScene gameScene;
    private boolean connected;
    private int PORT;

    public static void main(String args[]) throws Exception {
        launch(args);
    }

    public String parseNick(String nickWithScore) {
        String[] parts = nickWithScore.split(" ");
        String part1 = parts[0];
        String part2 = parts[1];
        String dots = "";
        String showable = dots + part2;

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

    private void showConnecionPopup() {

        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Connection");
        dialog.setHeaderText("Please type ip adress and port");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("img/Snake-icon.png"));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 15, 10, 10));

        TextField adress = new TextField();
        adress.setPromptText("Adress");
        TextField port = new TextField();
        port.setPromptText("Port");

        grid.add(new Label("Ip:"), 0, 0);
        grid.add(adress, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(port, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        adress.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> adress.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(adress.getText(), port.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(data -> {
            ip = data.getKey().replaceAll("\\s+", "");
            try {
                PORT = Integer.parseInt(data.getValue());
            } catch (NumberFormatException e) {
                popupMessage("Adress and port need to be numbers!" + e.toString());
                connected = false;
            }
        });

        try {
            Socket clientSocket = new Socket(ip, PORT);
            clientSocket.close();
            connected = true;
        } catch (IOException e) {
            popupMessage("Sever with ip '" + ip + "' doesn't exist! "
                    + "\n" + e.toString());
            connected = false;
        } catch (NumberFormatException e) {
            popupMessage("Adress and port need to be numbers!" + e.toString());
            connected = false;
        }
    }

    private void popupMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setContentText(msg);
        alert.show();
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
            String dots = "";
            String showable = dots + part2;

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
        primaryStage.getIcons().add(new Image("img/Snake-icon.png"));
        showConnecionPopup();

        if (connected) {

            primaryStage.setTitle("Snake");
            primaryStage.setResizable(false);

            BorderPane root = new BorderPane();
            gameScene = new GameScene();
            Pane settingBar = new Pane();
            root.setPadding(new Insets(10, 20, 10, 20));

            Button reset = new Button("Try again");
            Button start = new Button("Start");

            reset.setPrefSize(65, 20);
            start.setPrefSize(65, 20);

            ListView<String> players = new ListView<>();
            players.getStyleClass().add("scoreboard-font");

            ObservableList<String> items = FXCollections.observableArrayList();
            players.setItems(items);
            players.setLayoutY(50);
            players.setPrefHeight(550);
            players.setMaxWidth(150);
            players.setFocusTraversable(false);

            Scene scene = new Scene(root, 1000, 620);
            reset.setLayoutX(85);

            settingBar.getChildren().addAll(reset, start, players);

            start.setOnAction(event -> {
                reset.setDisable(true);
                start.setDisable(true);

                String fromServer;
                items.clear();

                try {
                    Socket clientSocket = new Socket(ip, PORT);
                    if (nick == null)
                        showNickPopup();
                    gameScene.getTimer().start();

                    primaryStage.setOnCloseRequest(e -> {
                        if (gameScene.isDead()) {
                            Platform.exit();
                            System.exit(0);
                        } else {
                            e.consume();
                        }
                    });

                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

                    fromServer = inFromServer.readLine();
                    System.out.println("Recieved from server: " + fromServer);

                    output.println(nick);
                    System.out.println("Sent to server: " + nick);

                    fromServer = inFromServer.readLine();
                    System.out.println("Recieved from server: " + fromServer);

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
                                        System.out.println("Sent to server: " + gameScene.getLastScore());

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

                } catch (ConnectException e) {
                    popupMessage(e.toString());
                    primaryStage.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException w) {
                    popupMessage(w.toString());
                    primaryStage.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
            popupMessage("Connected!");
        }
    }
}
