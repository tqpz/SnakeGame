import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;

class EventHandler extends Thread {
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
        String line;
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.println("Connected to the server");

            nick = input.readLine();

            items.add(nick);

            output.println(items);

            score = input.readLine();

//            while(score.equals("0")){
//                Thread.sleep(1000);
//                //System.out.println("null");
//            }

            System.out.println(score);
            items.remove(items.size() - 1);
            items.add(nick + " " + score);

            output.println(items);

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