package reactor.client;

import java.io.IOException;

public class Controller {
    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", 3000);
            Thread clientThread = new Thread(client);
            clientThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
