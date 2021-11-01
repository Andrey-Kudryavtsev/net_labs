package ru.nsu.kudryavtsev.andrey.Client;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import ru.nsu.kudryavtsev.andrey.Protocol;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger("CLIENT");
    private static final int TIMEOUT_MS = 10000;
    private final Socket tcpSocket;
    private final String filepath;

    public Client(String filepath, String serverAddress, int serverPort) throws IOException {
        this.filepath = filepath;
        tcpSocket = new Socket(serverAddress, serverPort);
        tcpSocket.setSoTimeout(TIMEOUT_MS);
        logger.info("Socket was opened. ip: " + serverAddress + "; port: " + serverPort);
    }

    public void start() throws IOException {
        try {
            var inputStream = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            logger.info("Start uploading the file");
            System.out.println("Start uploading the file");
            Protocol.upload(filepath, tcpSocket.getOutputStream());
            printResultOfDownload(inputStream);
        } catch (SocketTimeoutException e) {
            logger.warn("Fail to upload the file: server doesn't respond");
            System.out.println("Fail to upload the file: server doesn't respond");
        } finally {
            tcpSocket.close();
            logger.info("Socket was closed");
        }
    }

    private void printResultOfDownload(BufferedReader inputStream) throws IOException {
        String resultOfUpload = inputStream.readLine();
        switch (resultOfUpload) {
            case "SUCCESS" -> {
                logger.info("File was successfully uploaded");
                System.out.println("File was successfully uploaded");
            }
            case "FAILURE" -> {
                logger.info("Fail to upload the file");
                System.out.println("Fail to upload the file");
            }
            default -> {
                logger.warn("Fail to upload the file: unknown server response");
                System.out.println("Fail to upload the file: unknown server response");
            }
        }
    }
}
