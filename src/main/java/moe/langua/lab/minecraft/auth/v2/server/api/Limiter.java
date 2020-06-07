package moe.langua.lab.minecraft.auth.v2.server.api;

import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Limiter {
    private final long LIMIT;
    private final ConcurrentHashMap<Object, Long> usageRecord = new ConcurrentHashMap<>();
    private long nextReset = 0;

    public Limiter(long limit, long periodInMilliseconds, String handlerHandlePath) {
        LIMIT = limit;
        if (limit > 0) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    usageRecord.keySet().forEach((target) -> {
                        long times = usageRecord.get(target);
                        if (times > 10 && times > limit * 2) {
                            Utils.logger.log(LogRecord.Level.WARN, target.toString() + " tried to get " + handlerHandlePath + " for " + times + " times with in the last usage reset circle (" + periodInMilliseconds / 1000.0 + " seconds).");
                        }
                    });
                    usageRecord.clear();
                    nextReset = System.currentTimeMillis() + periodInMilliseconds;
                }
            }, 0, periodInMilliseconds);
        }
    }

    public boolean getUsability(Object target) {
        if (LIMIT < 0) return true;
        if (!usageRecord.containsKey(target)) usageRecord.put(target, 0L);
        return usageRecord.get(target) < LIMIT;
    }

    public void add(Object target, long delta) {
        if (!usageRecord.containsKey(target)) usageRecord.put(target, 0L);
        usageRecord.put(target, usageRecord.get(target) + delta);
    }

    public long getLIMIT(){
        return LIMIT;
    }

    public long getNextReset() {
        return nextReset;
    }
}
