package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Reactor implements Runnable {
    final Selector selector;
    final ServerSocketChannel socketChannel;

    public Reactor(int port) throws IOException {
        selector = Selector.open();
        socketChannel = ServerSocketChannel.open();
        socketChannel.socket().bind(new InetSocketAddress(port));
        socketChannel.configureBlocking(false);
        // 通道注册接受事件
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor());
    }

    class Acceptor implements Runnable {
        @Override
        public void run() {
            try {
                SocketChannel acceptorSocketChannel = socketChannel.accept();
                if (acceptorSocketChannel != null) {
                    new Handler(selector, acceptorSocketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    final class Handler implements Runnable{
        final SocketChannel socketChannel;
        final SelectionKey selectionKey;
        ByteBuffer input = ByteBuffer.allocate(1024);
        ByteBuffer output = ByteBuffer.allocate(1024);
        static final int READING=0, SENDING=1;
        int state = READING;


        Handler(Selector selector, SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            selectionKey = socketChannel.register(selector, 0);
            // 这部分干嘛的??
            selectionKey.attach(this);
            selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        boolean inputIsComplete() {
          return true;
        }

        boolean outputIsComplete() {
            return true;
        }
        void process() { }

        @Override
        public void run() {
            try {
                if (state == READING) read();
                if (state == SENDING) send();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void read() throws IOException{
            socketChannel.read(input);
            if (inputIsComplete()) {
                process();
                state = SENDING;
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
        }

        void send() throws IOException{
            socketChannel.write(output);
            selectionKey.cancel();
        }
    }


    // run 方法定义了 dispatch loop基本行为
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                // 轮询查询
                Set selected = selector.selectedKeys();
                Iterator iterator = selected.iterator();
                while (iterator.hasNext()) {
                    // 这里为何要强制转为SelectionKey??
                    dispatch((SelectionKey) iterator.next());
                    selected.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        // 用Runnable接口来强制转Object 为 Runnable?
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }
}

