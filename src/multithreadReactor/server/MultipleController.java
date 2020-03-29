package multithreadReactor.server;


import java.io.IOException;

public class MultipleController {
    public static void main(String[] args) {
        try {
            int port = 3000;
            Reactor server = new Reactor(port);
            Thread serverThread = new Thread(server);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

