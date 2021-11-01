package ru.nsu.kudryavtsev.andrey;

import org.slf4j.Logger;

public class Validator {
    private static final int MAX_PORT_NUM = 65535;

    public enum AppType {
        SERVER,
        CLIENT
    }

    public static boolean isInputValid(String[] args, AppType appType, Logger logger) {
        switch (appType) {
            case SERVER -> {
                if (args.length < 1) {
                    logger.warn("Not enough arguments for server application");
                    return false;
                }
                if (Integer.parseInt(args[0]) < 0 || Integer.parseInt(args[0]) > MAX_PORT_NUM) {
                    logger.warn("Wrong port argument:   " + args[0]);
                    return false;
                }
            }
            case CLIENT -> {
                if (args.length < 3) {
                    logger.warn("Not enough arguments for client application");
                    return false;
                }
                if (args[0].length() > 4096) {
                    logger.warn("File name is too long:\n   " + args[0]);
                    return false;
                }
                if (Integer.parseInt(args[2]) < 0 || Integer.parseInt(args[2]) > MAX_PORT_NUM) {
                    logger.warn("Wrong port argument:   " + args[2]);
                    return false;
                }
            }
            default -> {
                logger.warn("Unexpected argument:   " + args[0]);
                return false;
            }
        }
        return true;
    }
}
