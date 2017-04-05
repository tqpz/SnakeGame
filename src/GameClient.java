import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameClient extends Application {
    private static boolean closed = false;
    private String nick;
    private GameScene gameScene;

    public static void main(String args[]) throws Exception {
        launch(args);
    }

    private void showNickPopup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hello!");
        dialog.setHeaderText("Register");
        dialog.setContentText("Please enter your name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            nick = name;
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
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Snake");
        primaryStage.setResizable(false);

        BorderPane root = new BorderPane();
        gameScene = new GameScene();
        Pane settingBar = new Pane();
        root.setPadding(new Insets(10, 20, 10, 20));

        Button reset = new Button("Reset");
        Button start = new Button("Start");

        ListView<String> players = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();
        players.setItems(items);
        players.setLayoutY(50);
        players.setMaxHeight(400);
        players.setMaxWidth(150);
        players.setFocusTraversable(false);

        Scene scene = new Scene(root, 1000, 620);

        reset.setLayoutX(50);

        settingBar.getChildren().addAll(reset, start, players);

        start.setOnAction(event -> gameScene.getTimer().start());
        reset.setOnAction(event -> gameScene.resetAll());

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

        primaryStage.setScene(scene);
        primaryStage.show();

        String fromServer;

        Socket clientSocket = new Socket("localhost", 4444);
        showNickPopup();
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

        fromServer = inFromServer.readLine();
        System.out.println(fromServer);
        output.println(nick);

        fromServer = inFromServer.readLine();

        parseInputString(fromServer, items);

        Thread someThread = new Thread() {
            @Override
            public void run() {
                while (!gameScene.isDead()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                output.println(gameScene.getLastScore());
            }
        };

        someThread.start();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
            closed = true;
        });
    }
}
