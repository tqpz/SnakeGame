import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class Server extends Application {
    private final static int PORT = 4444;
    private static ServerSocket server;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<String> items = FXCollections.observableArrayList();

        BorderPane root = new BorderPane();

        Pane panel = new Pane();

        TextArea screen = new TextArea();
        Button start = new Button("Start");
        Button stop = new Button("Stop");


        panel.getChildren().addAll(start);
        root.getChildren().addAll(panel, screen);

        //root.setCenter(panel);
        //root.setBottom(screen);

        Task task = new Task<Void>() {
            int connectionNumber = 1;
            ObservableList<String> items = FXCollections.observableArrayList();

            @Override
            public Void call() throws Exception {
                server = new ServerSocket(PORT);
                System.out.println("Server started with port: " + PORT);
                while (true) {
                    Socket socket = server.accept();
                    InetAddress addr = socket.getInetAddress();
                    System.out.println("Connection number: " + connectionNumber + " from adress: "
                            + addr.getHostName() + " ["
                            + addr.getHostAddress() + "]");
                    new EventHandler(socket, connectionNumber, items).start();
                    connectionNumber++;
                }

            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);

        start.setOnAction(event -> th.start());


        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();
    }
}

