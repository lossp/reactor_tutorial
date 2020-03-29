package multithreadReactor.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadHandler implements Runnable{
    private static int THREAD_NUMBER = 10;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_NUMBER);
    private final SocketChannel socketChannel;
    private final Selector selector;
    private SelectionKey selectionKey;
    private static final int READING = 0, WRITING = 1;
    private static int state = READING;
    ByteBuffer input = ByteBuffer.allocate(1024);
    ByteBuffer output = ByteBuffer.allocate(128);

    public MultiThreadHandler(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        this.selector = selector;
        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
    }
    @Override
    public void run() {
        if (state == READING) read();
        if (state == WRITING) write();
    }

    void read() {
            threadPool.submit(() -> {
                try {
                    readProcess();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    void write() {
        threadPool.submit(() -> {
            try {
                writeProcess();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    void readProcess() throws IOException{
        System.out.println("读处理中");
        socketChannel.read(input);
        input.flip();
        System.out.println("received: " + new String(input.array()));
        state = WRITING;
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        selectionKey.selector().wakeup();
    }
    void writeProcess() throws IOException{
        System.out.println("写处理中");
        if (!output.hasRemaining()) {
            selectionKey.cancel();
            return;
        }
        output.rewind();
        socketChannel.write(output);
        state = READING;
        selectionKey.interestOps(SelectionKey.OP_READ);
        selectionKey.selector().wakeup();
    }
}
