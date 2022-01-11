package handler;

import logger.MyLogger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import socks5.SOCKS5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
public class GreetingHandler extends Handler {
    private final Selector selector;

    @Override
    public void onRead(SelectableChannel channel) {
        SocketChannel clientChannel = (SocketChannel) channel;
        try {
            ByteBuffer buf = ByteBuffer.allocate(SOCKS5.MAX_GREETING_MESSAGE_LENGTH);
            int readBytes = clientChannel.read(buf);
            if (readBytes <= 0) {
                return;
            }

            buf.flip();
            byte protocolVersion = buf.get();
            if (!isExpectedByte("Incompatible SOCKS version", protocolVersion, SOCKS5.SOCKS5_VERSION)) {
                dropChannel(clientChannel, selector);
                return;
            }

            byte nAuth = buf.get();
            if (nAuth < SOCKS5.MIN_AUTH_NUMBER) {
                MyLogger.getLogger().info("No authentication methods supported by client");
                dropChannel(clientChannel, selector);
                return;
            }

            boolean expectedMethodSupported = false; // because we support only "no-auth" method
            for (int i = 0; i < Byte.toUnsignedInt(nAuth); i++) {
                if (isExpectedByte("Unsupported authentication method", buf.get(), SOCKS5.NO_AUTH)) {
                    expectedMethodSupported = true;
                    break;
                }
            }
            if (!expectedMethodSupported) {
                MyLogger.getLogger().error("No-auth method is not supported by client");
                dropChannel(clientChannel, selector);
                return;
            }

            clientChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            dropChannel(clientChannel, selector);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onWrite(SelectableChannel channel) {
        SocketChannel clientChannel = (SocketChannel) channel;
        try {
            ByteBuffer buf = ByteBuffer.allocate(SOCKS5.MAX_GREETING_MESSAGE_LENGTH);
            buf.put(SOCKS5.SOCKS5_VERSION);
            buf.put(SOCKS5.NO_AUTH);

            buf.flip();
            clientChannel.write(buf);
            clientChannel.register(selector, SelectionKey.OP_READ, new ConnectionRequestHandler(selector));
        } catch (IOException e) {
            dropChannel(clientChannel, selector);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }
}
