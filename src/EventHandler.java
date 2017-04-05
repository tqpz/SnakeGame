import java.io.*;
import java.net.Socket;

class EventHandler extends Thread {
    boolean retire = false;
    private Socket socket;
    private int connectionNumber;
    private PrintWriter output;
    private BufferedReader input;
    private String nick;

    public EventHandler(Socket socket, int connectionNumber) {
        this.socket = socket;
        this.connectionNumber = connectionNumber;
    }

    public void run() {
        String line;
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.println("Connected to the server");
            nick = input.readLine();

            System.out.println("Connection number: "
                    + connectionNumber + " disconnected");
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }

}