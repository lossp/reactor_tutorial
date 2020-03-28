package reactor.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

public class Server implements Runnable {
    final ServerSocketChannel serverSocketChannel;
    final Selector selector;

    public Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor(serverSocketChannel, selector));
    }

    @Override
    public void run() {
        System.out.println("Server is initializing");
        while (!Thread.interrupted()) {
            try {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                for (SelectionKey selectedItem: selected) {
                    dispatch(selectedItem);
                }
                selected.clear();

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null) runnable.run();
    }
}
