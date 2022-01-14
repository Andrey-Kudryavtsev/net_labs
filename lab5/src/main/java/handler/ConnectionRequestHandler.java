package handler;

import logger.MyLogger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import socks5.SOCKS5;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
public class ConnectionRequestHandler extends Handler {
    private final Selector selector;
    private SocketChannel client;
    private SocketChannel host;
    private ByteBuffer buf;

    @Override
    public void onRead(SelectableChannel channel) {
        client = (SocketChannel) channel;
        try {
            buf = ByteBuffer.allocate(SOCKS5.MAX_CONNECTION_REQUEST_MESSAGE_LENGTH);
            int readBytes = client.read(buf);
            if (readBytes <= 0) {
                return;
            }

            buf.flip();
            byte protocolVersion = buf.get();
            if (!isExpectedByte("Incompatible SOCKS version", protocolVersion, SOCKS5.SOCKS5_VERSION)) {
                dropChannel(client, selector);
                return;
            }

            byte cmd = buf.get();
            if (!isExpectedByte("Unsupported command code", cmd, SOCKS5.TCP_STREAM_CONNECTION)) {
                dropChannel(client, selector);
                return;
            }

            byte reserved = buf.get();
            if (!isExpectedByte("Missing reserved byte", reserved, SOCKS5.RESERVED)) {
                dropChannel(client, selector);
                return;
            }

            byte addrType = buf.get();
            if (!isExpectedByte("Unsupported address type", addrType, SOCKS5.IPV4_ADDRESS, SOCKS5.DOMAIN_NAME)) {
                dropChannel(client, selector);
                return;
            }

            switch (addrType) {
                case SOCKS5.IPV4_ADDRESS -> {
                    byte[] address = new byte[4];
                    buf.get(address);
                    short port = buf.getShort();
                    Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress(address);

                    host = SocketChannel.open();
                    host.configureBlocking(false);
                    host.connect(new InetSocketAddress(inet4Address, port));

                    client.keyFor(selector).interestOps(0); // not interested in client while host is not connected
                    host.register(selector, SelectionKey.OP_CONNECT, this);

                }
                case SOCKS5.DOMAIN_NAME -> {
                    byte domainNameLength = buf.get();
                    byte[] address = new byte[Byte.toUnsignedInt(domainNameLength)];
                    buf.get(address);
                    short port = buf.getShort();
                    client.register(selector, 0, new DomainResolveHandler(selector, client, new String(address), port));
                }
            }
        } catch (IOException e) {
            dropChannels(selector, client, host);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onWrite(SelectableChannel channel) {
        try {
            buf.flip();
            buf.put(1, SOCKS5.REQUEST_GRANTED);
            client.write(buf);
            TransmitHandler handler = new TransmitHandler(selector, client, host);
            client.register(selector, SelectionKey.OP_READ, handler);
            host.register(selector, SelectionKey.OP_READ, handler);
        } catch (IOException e) {
            dropChannels(selector, client, host);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onConnect(SelectableChannel channel) {
        try {
            host.finishConnect();
            client.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
            host.keyFor(selector).interestOps(0);
        } catch (IOException e) {
            dropChannels(selector, client, host);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }
}
