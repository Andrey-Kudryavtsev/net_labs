package handler;

import logger.MyLogger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.channels.*;

@RequiredArgsConstructor
public class AcceptHandler extends Handler {
    private final Selector selector;

    @Override
    public void onAccept(SelectableChannel channel) {
        SocketChannel clientChannel = null;
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
            clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, new GreetingHandler(selector));
        } catch (IOException e) {
            dropChannel(clientChannel, selector);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }
}
