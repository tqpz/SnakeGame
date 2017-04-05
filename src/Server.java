import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class Server {
    private final static int PORT = 4444;
    private static ServerSocket server;

    public static void main(String[] args) {
        int connectionNumber = 1;
        ObservableList<String> items = FXCollections.observableArrayList();

        try {
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
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

