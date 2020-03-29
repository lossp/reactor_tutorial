package basicReactor.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client implements Runnable {
    private final SocketChannel socketChannel;
    private final String ipAddress;
    private final int portNumber;
    private ByteBuffer input;
    private ByteBuffer output;


    Client(String ipAddress, int portNumber) throws IOException {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(ipAddress, portNumber));
        input = ByteBuffer.allocate(32);
        output = ByteBuffer.allocate(32);
    }

    private void process() throws IOException{
        input.put("Hello there".getBytes());
        input.flip();

        while (true) {
            input.rewind();
            socketChannel.write(input);
            output.clear();
            socketChannel.read(output);
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Client is initializing");
            process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
