package ru.nsu.kudryavtsev.andrey.Client;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.kudryavtsev.andrey.Validator;

import java.io.IOException;

public class MainClient {
    private static final Logger logger = LoggerFactory.getLogger("CLIENT");

    public static void main(String[] args) {
        if (!Validator.isInputValid(args, Validator.AppType.CLIENT, logger)) {
            return;
        }

        try {
            var client = new Client(args[0], args[1], Integer.parseInt(args[2]));
            client.start();
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
