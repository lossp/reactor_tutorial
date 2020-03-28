package reactor.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public final class Handler implements Runnable {
    private Selector selector;
    private final SocketChannel channel;
    private final SelectionKey selectionKey;
    ByteBuffer input = ByteBuffer.allocate(1024);
    ByteBuffer output = ByteBuffer.allocate(128);
    static final int READING = 0, SENDING = 1;
    int state = READING;

    Handler(Selector selector, SocketChannel channel) throws IOException {
        this.selector = selector;
        this.channel = channel;
        channel.configureBlocking(false);
        selectionKey = channel.register(selector, READING);
        selectionKey.attach(this);
        // interestOps作用?
        selectionKey.interestOps(SelectionKey.OP_READ);
        // wakeup作用？
        selector.wakeup();
    }
    @Override
    public void run() {
        try {
            if (state == READING) { read(); }
            if (state == SENDING) { send(); }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void read() throws IOException{
        channel.read(input);
        if (inputIsComplete()) {
            process();
            state = SENDING;
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void send() throws IOException{
        channel.write(output);
        if (outputIsComplete()) selectionKey.cancel();
    }

    private void process() {
        System.out.println("Processing");
    }


    private boolean inputIsComplete() {
        return true;
    }

    private boolean outputIsComplete() {
        return true;
    }
}