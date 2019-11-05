package com.jonnymatts.jzonbie.history;

public class CallHistorySnapshot {
    private final int count;
    private final int persistedCount;

    private CallHistorySnapshot(int count, int persistedCount) {
        this.count = count;
        this.persistedCount = persistedCount;
    }

    public static CallHistorySnapshot snapshot(int count, int persistedCount) {
        return new CallHistorySnapshot(count, persistedCount);
    }

    public int getCount() {
        return count;
    }

    public int getPersistedCount() {
        return persistedCount;
    }
}
