import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class Server {
    private static ServerSocket server;
    private final static int PORT = 4444;

    public static void main(String[] args) {
        int connectionNumber = 1;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started with port: " + PORT);
            while (true) {
                Socket socket = server.accept();
                InetAddress addr = socket.getInetAddress();
                System.out.println("Connection number: " + connectionNumber + " from adress: "
                        + addr.getHostName() + " ["
                        + addr.getHostAddress() + "]");
                new EventHandler(socket, connectionNumber).start();
                connectionNumber++;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

