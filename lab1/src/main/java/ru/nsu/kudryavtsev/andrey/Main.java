package ru.nsu.kudryavtsev.andrey;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            InetAddress groupAddress;
            if (args.length == 0)
            {
                groupAddress = InetAddress.getByName("224.1.1.1");
            } else
            {
                groupAddress = InetAddress.getByName(args[0]);
            }

            var multicaster = new Multicaster(groupAddress);
            multicaster.openMulticastSocket();
            long lastTimeForSend = System.currentTimeMillis();
            long lastTimeForCheckAlive = System.currentTimeMillis();
            long sendDelay = 2000;
            long checkAliveDelay = 10000;
            while (true)
            {
                long curTime = System.currentTimeMillis();
                if ((curTime - lastTimeForSend) >= sendDelay)
                {
                    multicaster.sendUUID();
                    lastTimeForSend = curTime;
                }

                int messageLength = 36;
                DatagramPacket receivedDatagram = multicaster.receive(messageLength);
                if (receivedDatagram != null)
                {
                    String receivedUUID = new String(receivedDatagram.getData(), 0, receivedDatagram.getLength());
                    var receivedDatagramInfo = new DatagramInfo(receivedDatagram.getAddress(), curTime);
                    if (multicaster.add(receivedUUID, receivedDatagramInfo) == null)
                    {
                        System.out.println("New app has connected:\n\t\t" + receivedUUID + ": " + receivedDatagram.getAddress().toString());
                        multicaster.printAppsAddresses();
                    }
                }

                if ((curTime - lastTimeForCheckAlive) >= checkAliveDelay)
                {
                    if (multicaster.removeDeadApps(curTime))
                    {
                        System.out.println("Some apps have died");
                        multicaster.printAppsAddresses();
                    }
                    if (multicaster.getAliveAppCount() == 1)
                    {
                        System.out.println("No other apps");
                        break;
                    }
                    lastTimeForCheckAlive = curTime;
                }
            }
            multicaster.shutDown();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
