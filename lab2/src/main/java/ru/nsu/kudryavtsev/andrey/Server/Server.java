package ru.nsu.kudryavtsev.andrey.Server;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import ru.nsu.kudryavtsev.andrey.SpeedInfo;
import ru.nsu.kudryavtsev.andrey.Protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger("SERVER");
    private static final int TIMEOUT = 10000;
    public static final long SPEED_CALC_INTERVAL_MS = 3000;
    public static final long SPEED_CALC_DELAY_MS = 3000;
    public static final int SINGLE_THREAD = 1;
    private static int LATEST_CLIENT_ID = 0;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        logger.info("\t\t\t//--- Server is up ---\\\\");
    }

    public void start() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket newConnection = serverSocket.accept();
            newConnection.setSoTimeout(TIMEOUT);
            int clientID = getClientID();
            logger.info("Client " + clientID + " -- New connection");

            threadPool.execute(() -> {
                this.downloadFile(newConnection, clientID);
            });
        }
    }

    private void downloadFile(Socket newConnection, int clientID) {
        var scheduledThreadPool = Executors.newScheduledThreadPool(SINGLE_THREAD);
        SpeedInfo speedInfo = new SpeedInfo();
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            this.printSpeed(clientID, SPEED_CALC_INTERVAL_MS, speedInfo);
        }, SPEED_CALC_DELAY_MS, SPEED_CALC_INTERVAL_MS, TimeUnit.MILLISECONDS);
        PrintWriter outputStream;

        try (newConnection) {
            outputStream = new PrintWriter(newConnection.getOutputStream(), true);
            logger.info("Start downloading the file from client " + clientID);
            boolean isDownloadSuccessful = Protocol.download("upload/", speedInfo, newConnection.getInputStream());
            sendResultMessage(isDownloadSuccessful, clientID, outputStream);
        } catch (SocketTimeoutException e) {
            logger.warn("Client " + clientID + " -- Doesn't respond");
            return;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return;
        } finally {
            scheduledThreadPool.shutdown();
            logger.info("Client " + clientID + " -- Connection closed");
        }
        long downloadInterval = System.currentTimeMillis() - speedInfo.getStartOfDownload();
        if (downloadInterval < SPEED_CALC_INTERVAL_MS) {
            printSpeed(clientID, downloadInterval, speedInfo);
        }
        printDownloadTime(clientID, downloadInterval);
    }

    private void printSpeed(int clientID, long interval, SpeedInfo speedInfo) {
        long curTime = System.currentTimeMillis();
        if (!speedInfo.isEndOfDownload() || (curTime - speedInfo.getStartOfDownload()) < interval) {
            System.out.printf("""
                            Client %d --| Moment speed:  %.12f MB/s
                                   \t   | Average speed: %.12f MB/s
                                             
                            """,
                    clientID, speedInfo.getMomentSpeed(interval) / 1024 / 1024, speedInfo.getAvgSpeed(curTime) / 1024 / 1024
            );
        }
    }

    private void sendResultMessage(boolean isDownloadSuccessful, int clientID, PrintWriter outputStream) {
        if (isDownloadSuccessful) {
            outputStream.println("SUCCESS");
            logger.info("Client " + clientID + " -- File was successfully downloaded");
        } else {
            outputStream.println("FAILURE");
            logger.info("Client " + clientID + " -- Fail to download the file");
        }
    }

    private void printDownloadTime(int clientID, long downloadInterval) {
        System.out.printf("""
                Client %d --| Download takes %.6fs
                       \t   --------------------------
                """, clientID, ((float) downloadInterval / 1000));
    }

    private int getClientID() {
        return LATEST_CLIENT_ID++;
    }
}
