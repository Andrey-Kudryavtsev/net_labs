package ru.nsu.kudryavtsev.andrey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;

@RequiredArgsConstructor
public class DatagramInfo {
    @Getter
    private final InetAddress address;
    @Getter
    private final long timeOfResponse;
}
