package basicReactor.server;

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
                System.out.println("selected大小为: "  + selected.size());
                for (SelectionKey selectedItem: selected) {
                    printKeyInfo(selectedItem);
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

    private static void printKeyInfo(SelectionKey sk) {
        StringBuilder s = new StringBuilder();
        s.append("Att: " + (sk.attachment() == null ? "no" : "yes"));
        s.append(", isReadable: " + sk.isReadable());
        s.append(", isAcceptable: " + sk.isAcceptable());
        s.append(", isConnectable: " + sk.isConnectable());
        s.append(", isWritable: " + sk.isWritable());
        s.append(", Valid: " + sk.isValid());
        s.append(", Ops: " + sk.interestOps());
        System.out.println(s.toString());
    }
}
