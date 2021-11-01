package ru.nsu.kudryavtsev.andrey.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.kudryavtsev.andrey.Validator;

import java.io.IOException;

public class MainServer {
    private static final Logger logger = LoggerFactory.getLogger("SERVER");

    public static void main(String[] args) {
        if (!Validator.isInputValid(args, Validator.AppType.SERVER, logger)) {
            return;
        }

        try {
            var server = new Server(Integer.parseInt(args[0]));
            server.start();
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
