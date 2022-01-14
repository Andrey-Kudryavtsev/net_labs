import handler.AcceptHandler;
import handler.Handler;
import logger.MyLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class SocksProxyServer {
    private int port;

    public SocksProxyServer(int port) {
        this.port = port;
    }

    public void start() {
        Selector selector;
        ServerSocketChannel server;
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));
            server.register(selector, SelectionKey.OP_ACCEPT, new AcceptHandler(selector));
        } catch (IOException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
            return;
        }

        while (true) {
            try {
                if (selector.select() <= 0) {
                    continue;
                }
            } catch (IOException e) {
                MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
            }

            var selectedKeys = selector.selectedKeys();
            var iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                if (key.isValid() && key.isAcceptable()) {
                    if (key.attachment() == null) {
                        MyLogger.getLogger().warn("Key has no attachment");
                        continue;
                    }
                    ((Handler) key.attachment()).onAccept(key.channel());
                }
                if (key.isValid() && key.isReadable()) {
                    if (key.attachment() == null) {
                        MyLogger.getLogger().warn("Key has no attachment");
                        continue;
                    }
                    ((Handler) key.attachment()).onRead(key.channel());
                }
                if (key.isValid() && key.isWritable()) {
                    if (key.attachment() == null) {
                        MyLogger.getLogger().warn("Key has no attachment");
                        continue;
                    }
                    ((Handler) key.attachment()).onWrite(key.channel());
                }
                if (key.isValid() && key.isConnectable()) {
                    if (key.attachment() == null) {
                        MyLogger.getLogger().warn("Key has no attachment");
                        continue;
                    }
                    ((Handler) key.attachment()).onConnect(key.channel());
                }
                iterator.remove();
            }
        }
    }
}
