package ru.nsu.kudryavtsev.andrey;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class Multicaster {
    private static final int TIMEOUT = 1;
    private final UUID uuid = UUID.randomUUID();
    private final int multicastSocketPort = 8000;
    private final HashMap<String, DatagramInfo> timesOfResponse = new HashMap<>(10);
    private final InetAddress groupAddress;
    private MulticastSocket multicastSocket = null;

    public Multicaster(InetAddress groupAddress) throws IOException {
        this.groupAddress = groupAddress;
        openMulticastSocket();
    }

    private void openMulticastSocket() throws IOException {
        multicastSocket = new MulticastSocket(multicastSocketPort);
        multicastSocket.setSoTimeout(TIMEOUT);
        multicastSocket.joinGroup(groupAddress);
    }

    public void send(String content) throws IOException {
        if (content == null || multicastSocket == null) {
            return;
        }
        var datagram = new DatagramPacket(content.getBytes(), content.getBytes().length, groupAddress, multicastSocketPort);
        multicastSocket.send(datagram);
    }

    public void sendUUID() throws IOException {
        send(uuid.toString());
    }

    public DatagramPacket receive(int length) throws IOException {
        try {
            if (multicastSocket == null) {
                return null;
            }
            var receivedDatagram = new DatagramPacket(new byte[length], length);
            multicastSocket.receive(receivedDatagram);
            return receivedDatagram;
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    public boolean removeDeadApps(long curTime) {
        boolean isAnyDead = false;
        long maxTimeOfResponse = 10000;
        Iterator<String> iterator = timesOfResponse.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            long timeOfResponse = timesOfResponse.get(key).getTimeOfResponse();
            if ((curTime - timeOfResponse) >= maxTimeOfResponse) {
                iterator.remove();
                isAnyDead = true;
            }
        }
        return isAnyDead;
    }

    public int getAliveAppCount() {
        return timesOfResponse.size();
    }

    public DatagramInfo add(String uuid, DatagramInfo datagramInfo) {
        if (uuid == null || datagramInfo == null) {
            throw new IllegalArgumentException();
        }
        return timesOfResponse.put(uuid, datagramInfo);
    }

    public void printAppsAddresses() {
        System.out.println("Alive apps:\t\t\tUUID\t\t\t\tADDRESS");
        for (String key : timesOfResponse.keySet()) {
            if (uuid.toString().equals(key)) {
                System.out.println("(current)\t" + key + ": " + timesOfResponse.get(key).getAddress().toString());
            } else {
                System.out.println("\t\t" + key + ": " + timesOfResponse.get(key).getAddress().toString());
            }
        }
    }

    public void shutDown() throws IOException {
        if (multicastSocket == null) {
            return;
        }
        multicastSocket.leaveGroup(groupAddress);
        multicastSocket.close();
    }
}
