package handler;

import logger.MyLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public abstract class Handler {
    public void onAccept(SelectableChannel channel) {}
    public void onRead(SelectableChannel channel) {}
    public void onWrite(SelectableChannel channel) {}
    public void onConnect(SelectableChannel channel) {}

    protected boolean isExpectedByte(String msg, byte actual, byte... expected) {
        StringBuilder errmsg = new StringBuilder(msg + ". Expected ");
        for (byte b : expected) {
            errmsg.append(String.format("0x%02X, ", b));
            if (actual == b) {
                return true;
            }
        }
        errmsg.append(String.format("got 0x%02X", actual));
        MyLogger.getLogger().error(errmsg.toString());
        return false;
    }

    protected void dropChannel(SelectableChannel channel, Selector selector) {
        if (channel == null || selector == null) {
            return;
        }
        try {
            SelectionKey key = channel.keyFor(selector);
            if (key == null) {
                return;
            }
            channel.keyFor(selector).cancel();
            channel.close();
        } catch (IOException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    protected void dropChannels(Selector selector, SelectableChannel... channels) {
        for (var channel : channels) {
            dropChannel(channel, selector);
        }
    }
}
