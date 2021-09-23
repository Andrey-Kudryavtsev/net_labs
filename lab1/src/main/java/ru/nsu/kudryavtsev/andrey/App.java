package ru.nsu.kudryavtsev.andrey;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class App {
    private static final long SEND_DELAY = 2000;
    private static final long CHECK_ALIVE_DELAY = 10000;
    private static final int MESSAGE_LENGTH = 36;
    private static final int ALONE = -1;

    private final Multicaster multicaster;

    public App(InetAddress groupAddress) throws IOException {
        multicaster = new Multicaster(groupAddress);
    }

    public void start() throws IOException {
        long lastTimeForSend = System.currentTimeMillis();
        long lastTimeForCheckAlive = System.currentTimeMillis();
        while (lastTimeForCheckAlive != ALONE) {
            long curTime = System.currentTimeMillis();
            lastTimeForSend = sendUUIDWithDelay(curTime, lastTimeForSend);

            receiveUUID(curTime);

            lastTimeForCheckAlive = checkAliveAppsWithDelay(curTime, lastTimeForCheckAlive);
        }
        multicaster.shutDown();
    }

    public long sendUUIDWithDelay(long curTime, long lastTimeForSend) throws IOException {
        if ((curTime - lastTimeForSend) >= SEND_DELAY) {
            multicaster.sendUUID();
            return curTime;
        }
        return lastTimeForSend;
    }

    public void receiveUUID(long curTime) throws IOException {
        DatagramPacket receivedDatagram = multicaster.receive(MESSAGE_LENGTH);
        if (receivedDatagram != null) {
            String receivedUUID = new String(receivedDatagram.getData(), 0, receivedDatagram.getLength());
            var receivedDatagramInfo = new DatagramInfo(receivedDatagram.getAddress(), curTime);
            if (multicaster.add(receivedUUID, receivedDatagramInfo) == null) {
                System.out.println("New app has connected:\n\t\t" + receivedUUID + ": " + receivedDatagram.getAddress().toString());
                multicaster.printAppsAddresses();
            }
        }
    }

    public long checkAliveAppsWithDelay(long curTime, long lastTimeForCheckAlive) {
        if ((curTime - lastTimeForCheckAlive) >= CHECK_ALIVE_DELAY) {
            if (multicaster.removeDeadApps(curTime)) {
                System.out.println("Some apps have died");
                multicaster.printAppsAddresses();
            }
            if (multicaster.getAliveAppCount() == 1) {
                System.out.println("No other apps");
                return -1;
            }
            return curTime;
        }
        return lastTimeForCheckAlive;
    }
}
