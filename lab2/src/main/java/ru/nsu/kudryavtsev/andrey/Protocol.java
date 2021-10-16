package ru.nsu.kudryavtsev.andrey;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ru.nsu.kudryavtsev.andrey.Client.Client.Protocol structure:
 * <p>
 * 1) 4 byte - length of filename
 * 2) filename
 * 3) 8 byte - size of file
 * 4) file itself
 */
public class Protocol {
    private static final Logger serverLogger = LoggerFactory.getLogger("SERVER");
    private static final Logger clientLogger = LoggerFactory.getLogger("CLIENT");
    public static final int BUF_SIZE = 4096;

    public static void upload(String filename, OutputStream outputStream) throws IOException {
        var file = new File(filename);
        sendFileInfo(filename, file, outputStream);
        clientLogger.info("Filename length, filename and file size was sent to server");
        long actualFileSize = sendFile(new FileInputStream(file), outputStream);
        clientLogger.info("File was sent to server.   Actual file size = " + actualFileSize);
    }

    public static boolean download(String uploadDir, SpeedInfo speedInfo, InputStream inputStream) throws IOException {
        int filenameLength = getFilenameLength(speedInfo, inputStream);
        serverLogger.info("Got filename length from client:   filename length = " + filenameLength);
        String filename = getFilename(filenameLength, speedInfo, inputStream);
        serverLogger.info("Got filename from client:   filename = " + filename);
        long fileSize = getFileSize(speedInfo, inputStream);
        serverLogger.info("Got file size from client:   file size = " + fileSize);
        if (fileSize == 0) {
            serverLogger.info("File size equals zero");
            return false;
        }
        long actualFileSize = getFile(uploadDir, filename, fileSize, speedInfo, inputStream);
        serverLogger.info("Got file from client:   actual file size = " + actualFileSize);
        if (fileSize != actualFileSize) {
            serverLogger.info("File size != read file size:   " + fileSize + " != " + actualFileSize);
            return false;
        }
        return true;
    }

    private static void sendFileInfo(String filename, File file, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[Integer.BYTES + filename.length() + Long.BYTES];
        ByteBuffer bufWrapper = ByteBuffer.wrap(buf);

        bufWrapper.putInt(filename.length());
        clientLogger.info("Put filename length in buffer:   filename length = " + filename.length());
        bufWrapper.put(Integer.BYTES, filename.getBytes(StandardCharsets.UTF_8));
        clientLogger.info("Put filename in buffer:   filename = " + filename);
        bufWrapper.putLong(Integer.BYTES + filename.length(), file.length());
        clientLogger.info("Put file size in buffer:   file size = " + file.length());

        outputStream.write(buf);
        outputStream.flush();
    }

    private static long sendFile(FileInputStream fileInputStream, OutputStream outputStream) throws IOException {
        long actualFileSize = 0;
        int readBytes;
        byte[] buf = new byte[BUF_SIZE];

        while ((readBytes = fileInputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, readBytes);
            outputStream.flush();
            actualFileSize += readBytes;
        }

        fileInputStream.close();
        return actualFileSize;
    }

    private static int getFilenameLength(SpeedInfo speedInfo, InputStream inputStream) throws IOException {
        byte[] buf = new byte[Integer.BYTES];

        speedInfo.setStartOfDownload(System.currentTimeMillis());
        int readBytes;
        if ((readBytes = inputStream.read(buf)) == -1) {
            throw new IOException("Can't read filename length");
        }

        speedInfo.updateReadBytes(readBytes);
        return ByteBuffer.wrap(buf).getInt();
    }

    private static String getFilename(int filenameLength, SpeedInfo speedInfo, InputStream inputStream) throws IOException {
        byte[] buf = new byte[filenameLength];

        int readBytes;
        if ((readBytes = inputStream.read(buf)) == -1) {
            throw new IOException("Can't read filename");
        }

        speedInfo.updateReadBytes(readBytes);
        return new String(buf, 0, filenameLength, StandardCharsets.UTF_8);
    }

    private static long getFileSize(SpeedInfo speedInfo, InputStream inputStream) throws IOException {
        byte[] buf = new byte[Long.BYTES];

        int readBytes;
        if ((readBytes = inputStream.read(buf)) == -1) {
            throw new IOException("Can't read file size");
        }

        speedInfo.updateReadBytes(readBytes);
        return ByteBuffer.wrap(buf).getLong();
    }

    private static String getOnlyFilename(String filename) {
        return new File(filename).getName();
    }

    private static File createFileInUploadDir(String uploadDirPath, String filename) throws IOException {
        File uploadDir = new File(uploadDirPath);
        if (!(uploadDir.isDirectory())) {
            uploadDir.mkdir();
        }

        String uploadPath = uploadDirPath + filename;
        File file = new File(uploadPath);
        String filenameWithoutExtension = filename.split("\\.", 2)[0];
        String fileExtension = "." + filename.split("\\.", 2)[1];
        for (int i = 0; !(file.createNewFile()); i++) {
            uploadPath = uploadDirPath + filenameWithoutExtension + "(" + (i + 1) + ")" + fileExtension;
            file = new File(uploadPath);
        }

        return new File(uploadPath);
    }

    private static long getFile(String uploadDirPath, String filename, long fileSize, SpeedInfo speedInfo, InputStream inputStream) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long actualFileSize = 0;
        File file = createFileInUploadDir(uploadDirPath, getOnlyFilename(filename));
        var fileOutputStream = new FileOutputStream(file);

        while (fileSize > 0) {
            int readBytes = inputStream.read(buf);
            speedInfo.updateReadBytes(readBytes);
            fileOutputStream.write(buf, 0, readBytes);
            fileOutputStream.flush();
            fileSize -= readBytes;
            actualFileSize += readBytes;
        }

        fileOutputStream.close();
        return actualFileSize;
    }
}
