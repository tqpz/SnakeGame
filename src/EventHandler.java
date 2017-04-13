import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;

import java.io.*;
import java.net.Socket;

class EventHandler<W extends Event> extends Thread {
    boolean retire = false;
    private Socket socket;
    private int connectionNumber;
    private PrintWriter output;
    private BufferedReader input;
    private String nick;
    private String score;
    private ObservableList<String> items = FXCollections.observableArrayList();

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

            items.add(nick + " " + 2);
//
            output.println(items);
            System.out.println("Sent to client: " + items);

            score = input.readLine();
            System.out.println("Get from server: " + score);

//            while(score.equals("0")){
//                Thread.sleep(1000);
//                //System.out.println("null");
//            }

            items.remove(items.size() - 1);
            items.add(nick + " " + score);

            output.println(items);
            System.out.println("Sent to client: " + items);


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