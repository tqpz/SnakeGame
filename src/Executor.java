/**
 * Created by Mateusz on 16.04.2017.
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Executor {

    private static ServerSocket server;
    private Task<Void> task;
    Executor() {
        task = new Task<Void>() {
            int PORT = ServerApplication.getPORT();
            int connectionNumber = 1;
            ObservableList<String> items = FXCollections.observableArrayList();

            @Override
            public Void call() throws Exception {
                server = new ServerSocket(PORT);
                ServerApplication.appendOnScreen("Server started with port: " + PORT + "\n");
                while (true) {
                    Socket socket = server.accept();
                    InetAddress addr = socket.getInetAddress();
                    ServerApplication.appendOnScreen("Connection number: " + connectionNumber + " from adress: "
                            + addr.getHostName() + " ["
                            + addr.getHostAddress() + "]" + "\n");
                    new EventHandler(socket, connectionNumber, items).start();
                    connectionNumber++;
                }

            }
        };
    }

    public static ServerSocket getServer() {
        return server;
    }

    public void start() {
        try {
            Thread th = new Thread(task);
            th.start();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}