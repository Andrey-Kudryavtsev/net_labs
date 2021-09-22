package ru.nsu.kudryavtsev.andrey;

import java.net.InetAddress;

public class DatagramInfo
{
    private final InetAddress address;
    private final long timeOfResponse;

    public DatagramInfo(InetAddress address, long timeOfResponse)
    {
        this.address = address;
        this.timeOfResponse = timeOfResponse;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public long getTimeOfResponse()
    {
        return timeOfResponse;
    }
}
