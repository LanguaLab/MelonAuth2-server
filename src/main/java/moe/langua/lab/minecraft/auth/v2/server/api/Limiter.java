package moe.langua.lab.minecraft.auth.v2.server.api;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Limiter {
    private final int LIMIT;
    private final ConcurrentHashMap<InetAddress, Integer> usageRecord = new ConcurrentHashMap<>();
    private long nextReset = 0;

    public Limiter(int limit, long periodInMilliseconds) {
        LIMIT = limit;
        if (limit > 0) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    usageRecord.clear();
                    nextReset = System.currentTimeMillis() + periodInMilliseconds;
                }
            }, 0, periodInMilliseconds);
        }
    }

    public boolean getUsability(InetAddress address) {
        if (LIMIT < 0) return true;
        if (!usageRecord.containsKey(address)) usageRecord.put(address, 0);
        return usageRecord.get(address) < LIMIT;
    }

    public void add(InetAddress address, int delta) {
        if (!usageRecord.containsKey(address)) usageRecord.put(address, 0);
        usageRecord.put(address, usageRecord.get(address) + delta);
    }

    public boolean getUsabilityAndAdd1(InetAddress address) {
        add(address, 1);
        if (LIMIT < 0) return true;
        return getUsability(address);
    }

    public long getNextReset() {
        return nextReset;
    }
}
