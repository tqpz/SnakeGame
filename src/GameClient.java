import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameClient extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);
        GameScene gameScene = new GameScene();

        //set primary scene and show it
        root.setCenter(gameScene);
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.DOWN)
                gameScene.setLastDirection(KeyCode.DOWN);
            else if(key.getCode() == KeyCode.UP)
                gameScene.setLastDirection(KeyCode.UP);
            else if(key.getCode() == KeyCode.LEFT)
                gameScene.setLastDirection(KeyCode.LEFT);
            else if(key.getCode() == KeyCode.RIGHT)
                gameScene.setLastDirection(KeyCode.RIGHT);
        });

        String sentence;
        String modifiedSentence;

        Socket clientSocket = new Socket("localhost", 4444);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        modifiedSentence = inFromServer.readLine();
        System.out.println(modifiedSentence);
        clientSocket.close();
    }

    public static void main(String args[]) throws Exception {
        launch(args);
    }
}
