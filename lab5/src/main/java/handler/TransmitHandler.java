package handler;

import logger.MyLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TransmitHandler extends Handler {
    private final Selector selector;
    private final SocketChannelWrapper clientChannelWrap;
    private final SocketChannelWrapper hostChannelWrap;
    private SocketChannelWrapper curChannelWrap;
    private SocketChannelWrapper otherChannelWrap;

    public TransmitHandler(Selector selector, SocketChannel client, SocketChannel host) {
        this.selector = selector;
        clientChannelWrap = new SocketChannelWrapper(client);
        hostChannelWrap = new SocketChannelWrapper(host);
    }

    @Override
    public void onRead(SelectableChannel channel) {
        curChannelWrap = selectCurrentChannel(channel);
        otherChannelWrap = selectOtherChannel(channel);
        if (curChannelWrap == null || otherChannelWrap == null) {
            throw new RuntimeException("Neither client nor host channel");
        }
        try {
            int byteRead = curChannelWrap.getChannel().read(curChannelWrap.getBuf());
            if (byteRead > 0 && otherChannelWrap.getChannel().isConnected()) { // если прочитали с текущего канала, а другой подключен
                otherChannelWrap.addOption(SelectionKey.OP_WRITE, selector); // значит можем писать в другой канал
            }
            if (byteRead == -1) { // закончили чтение
                curChannelWrap.deleteOption(SelectionKey.OP_READ, selector); // значит больше не читаем с текущего канала
                curChannelWrap.setFinishRead(true); // ставим флаг завершения чтения
                if (curChannelWrap.getBuf().position() == 0) { // ничего не прочитали из канала сейчас, и в буфере тоже пусто
                    otherChannelWrap.getChannel().shutdownOutput(); // значит хватит написывать в другой канал
                    otherChannelWrap.setOutputShutdown(true); // ставим флаг завершения записи
                    if (curChannelWrap.isOutputShutdown() || otherChannelWrap.getBuf().position() == 0) { // если в придачу в этот канал уже ничего не пишем или в буфере другого канала пусто
                        dropChannels(selector, curChannelWrap.getChannel(), otherChannelWrap.getChannel()); // завершаем обмен данными
                    }
                }
            }

            if (!curChannelWrap.getBuf().hasRemaining()) { // если полностью заполнили буфер
                curChannelWrap.deleteOption(SelectionKey.OP_READ, selector); // перестаем читать из канала на время
            }
        } catch (IOException e) {
            dropChannel(curChannelWrap.getChannel(), selector);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }

    }

    @Override
    public void onWrite(SelectableChannel channel) {
        curChannelWrap = selectCurrentChannel(channel);
        otherChannelWrap = selectOtherChannel(channel);
        if (curChannelWrap == null || otherChannelWrap == null) {
            throw new RuntimeException("Either client nor host channel");
        }
        otherChannelWrap.getBuf().flip();
        try {
            int byteWrite = curChannelWrap.getChannel().write(otherChannelWrap.getBuf());
            if (byteWrite > 0) { // если что-то отправили
                otherChannelWrap.getBuf().compact();
                otherChannelWrap.addOption(SelectionKey.OP_READ, selector); // значит другой канал готов это прочитать
            }
            if (otherChannelWrap.getBuf().position() == 0) { // если у другого канала пустой буфер
                curChannelWrap.deleteOption(SelectionKey.OP_WRITE, selector); // значит нам нечего писать в текущий канал
                if (otherChannelWrap.isFinishRead()) { // если при этом другой канал закончил чтение
                    curChannelWrap.getChannel().shutdownOutput(); // значит перестаем пистаьв другой канал
                    curChannelWrap.setOutputShutdown(true); // устанавливаем флаг завершения записи в текущий канал
                    if (otherChannelWrap.isOutputShutdown()) { // если еще и запись в другой канал завершена
                        dropChannels(selector, curChannelWrap.getChannel(), otherChannelWrap.getChannel()); // завершаем обмен данными
                    }
                }
            }
        } catch (IOException e) {
            dropChannel(curChannelWrap.getChannel(), selector);
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    private SocketChannelWrapper selectCurrentChannel(SelectableChannel channel) {
        if (channel.equals(clientChannelWrap.getChannel())) {
            return clientChannelWrap;
        } else if (channel.equals(hostChannelWrap.getChannel())) {
            return hostChannelWrap;
        } else {
            return null;
        }
    }

    private SocketChannelWrapper selectOtherChannel(SelectableChannel channel) {
        if (channel.equals(clientChannelWrap.getChannel())) {
            return hostChannelWrap;
        } else if (channel.equals(hostChannelWrap.getChannel())) {
            return clientChannelWrap;
        } else {
            return null;
        }
    }
}
