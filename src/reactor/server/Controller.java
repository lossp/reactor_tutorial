package reactor.server;

import java.io.IOException;

public class Controller {
    public static void main(String[] args) {
        try {
            int port = 3000;
            Server server = new Server(port);
            Thread serverThread = new Thread(server);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
