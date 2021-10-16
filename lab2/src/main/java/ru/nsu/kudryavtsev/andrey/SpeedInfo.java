package ru.nsu.kudryavtsev.andrey;

import lombok.Getter;
import lombok.Setter;

public class SpeedInfo {
    @Setter @Getter private long startOfDownload = 0;
    @Setter @Getter private boolean isEndOfDownload = false;
    private long totalReadBytes = 0;
    private long momentReadBytes = 0;

    private synchronized void updateMomentReadBytes(long momentReadBytes) {
        this.momentReadBytes += momentReadBytes;
    }

    public synchronized double getMomentSpeed(long interval) {
        long savedMomentReadBytes = momentReadBytes;
        momentReadBytes = 0;
        return savedMomentReadBytes / ((double) interval / 1000);
    }

    private synchronized void updateTotalReadBytes(long totalReadBytes) {
        this.totalReadBytes += totalReadBytes;
    }

    public synchronized double getAvgSpeed(long curTime) {
        return totalReadBytes / ((double)(curTime - startOfDownload) / 1000);
    }

    public synchronized void updateReadBytes(long readByes) {
        updateMomentReadBytes(readByes);
        updateTotalReadBytes(readByes);
    }
}
