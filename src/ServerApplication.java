import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class ServerApplication extends Application {
    private static int PORT;
    private static ServerSocket server;

    private static TextArea screen;

    public static void main(String[] args) {
        launch();
    }

    public static int getPORT() {
        return PORT;
    }

    public static void appendOnScreen(String msg) {
        screen.appendText(msg);
    }

    public HBox addHBox(Button buttonCurrent, Button buttonProjected, TextField port) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(30);
        hbox.setStyle("-fx-background-color: #336699;");

        buttonCurrent.setPrefSize(100, 20);

        buttonProjected.setPrefSize(100, 20);

        port.setPrefSize(200, 20);
        port.setPromptText("Set port");
        hbox.getChildren().addAll(buttonCurrent, buttonProjected, port);

        return hbox;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image("img/Snake-icon.png"));
        primaryStage.setTitle("Snake Server: " + Inet4Address.getLocalHost().getHostAddress());
        primaryStage.setResizable(false);

        BorderPane root = new BorderPane();
        Button start = new Button("Start");
        Button stop = new Button("Exit");
        TextField port = new TextField();

        HBox topBox = addHBox(start, stop, port);

        screen = new TextArea();
        screen.setEditable(false);
        screen.setWrapText(true);


        root.setTop(topBox);

        root.setCenter(screen);
        Executor[] ex = new Executor[1];


        start.setOnAction(event -> {
            start.setDisable(true);
            try {
                PORT = Integer.parseInt(port.getText());
                ex[0] = new Executor();
                ex[0].start();
                primaryStage.setTitle("Snake Server: " + Inet4Address.getLocalHost().getHostAddress() + ":" + PORT);
            } catch (Exception e) {
                start.setDisable(false);
                screen.appendText(e + "\n");
            }
        });

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stop.setOnAction(event -> {
            try {
                ex[0].getServer().close();
                screen.appendText("ServerApplication stopped working" + "\n");
                PORT = 0;
            } catch (IOException e) {
                screen.appendText(e.toString());
                e.printStackTrace();
            }

            Platform.exit();
            System.exit(0);

        });

        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();
    }
}

