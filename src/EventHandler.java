import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

class EventHandler extends Thread {
    private Socket socket;
    private int connectionNumber;

    public EventHandler(Socket socket, int connectionNumber) {
        this.socket = socket;
        this.connectionNumber = connectionNumber;
    }

    public void run() {
        try {
            PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            output.println("Connected to the server");

            System.out.println("Connection number: "
                    + connectionNumber + " disconnected");
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
