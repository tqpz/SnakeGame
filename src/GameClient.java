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

import java.io.*;
import java.net.Socket;
import java.util.Optional;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameClient extends Application implements Serializable {
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

//        TextArea scoreBoard = new TextArea();
//        scoreBoard.setMaxWidth(150);
//        scoreBoard.setMaxHeight(800);
//        scoreBoard.setLayoutY(60);
//        scoreBoard.setEditable(false);
//        scoreBoard.setFocusTraversable(false);
//        scoreBoard.focusedProperty().addListener((observable,  oldValue,  newValue) -> {
//            if(newValue){
//                gameScene.requestFocus(); // Delegate the focus to container
//                //firstTime.setValue(false); // Variable value changed for future references
//            }
//        });

        ListView<String> players = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList(
                "Mati10", "Stefan", "UltraKnur2000", "Korwin Mekka");
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


        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
            closed = true;
        });
    }
}
