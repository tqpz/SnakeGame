import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;

class EventHandler extends Thread {
    private Socket socket;
    private int connectionNumber;
    private PrintWriter output;
    private BufferedReader input;
    private String nick;
    private String score;
    private ObservableList<String> items;

    public EventHandler(Socket socket, int connectionNumber, ObservableList<String> items) {
        this.socket = socket;
        this.connectionNumber = connectionNumber;
        this.items = items;
    }

    public void run() {
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.println("Connected to the server");

            nick = input.readLine();
            System.out.println("Get from server: " + nick);

            if (nick == null)
                nick = "unnamed";

            items.add(nick + " " + 0);

            output.println(items);
            ServerApplication.appendOnScreen("Sent to client: " + items + "\n");

            if (score == null)
                score = "0";

            score = input.readLine();
            System.out.println("Get from server: " + score);

            items.remove(items.size() - 1);
            if (score != null && nick != null)
                items.add(nick + " " + score);

            output.println(items);
            ServerApplication.appendOnScreen("Sent to client: " + items + "\n");


            ServerApplication.appendOnScreen("Connection number: "
                    + connectionNumber + " disconnected" + "\n");

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