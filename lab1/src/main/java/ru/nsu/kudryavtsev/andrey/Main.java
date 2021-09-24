package ru.nsu.kudryavtsev.andrey;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    private static InetAddress validateGroupAddress(String[] args) {
        try {
            if (args.length == 0) {
                return InetAddress.getByName("224.1.1.1");
            }

            InetAddress groupAddress = InetAddress.getByName(args[0]);
            if (!groupAddress.isMulticastAddress()) {
                System.err.println("Not a multicast address");
                return null;
            }
            return groupAddress;
        } catch (UnknownHostException e) { // TODO: эта ошибка ловится не только в случае белиберды вместо адреса
            System.err.println("Not an address");
            return null;
        }
    }

    public static void main(String[] args) {
        InetAddress groupAddress = validateGroupAddress(args);
        if (groupAddress == null) {
            return;
        }

        try {
            var app = new App(groupAddress);
            app.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
