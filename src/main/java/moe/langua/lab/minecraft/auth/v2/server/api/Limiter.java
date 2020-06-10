package moe.langua.lab.minecraft.auth.v2.server.api;

import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Limiter<ObjectType> {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final AtomicInteger offset = new AtomicInteger(0);
    private final long LIMIT;
    private final ConcurrentHashMap<ObjectType, Long> usageRecord = new ConcurrentHashMap<>();
    private long nextReset = 0;

    public Limiter(long limit, long periodInMilliseconds, String handlerHandlePath) {
        LIMIT = limit;
        if (limit < 0) return;

        long now = System.currentTimeMillis();
        long circled = (now / periodInMilliseconds);
        nextReset = (circled + 1) * periodInMilliseconds + offset.getAndAdd(50);
        long firstDelay = nextReset - now;
        if (firstDelay < 0) firstDelay = 0;
        scheduler.scheduleAtFixedRate(() -> {
            for (ObjectType x : usageRecord.keySet()) {
                long times = usageRecord.get(x);
                if (times > 10 && times > (limit * 2)) {
                    Utils.logger.log(LogRecord.Level.WARN, x.toString() + " tried to get " + handlerHandlePath + " for " + times + " times with in the last usage reset circle (" + periodInMilliseconds / 1000.0 + " seconds).");
                }
            }
            usageRecord.clear();
            nextReset += periodInMilliseconds;
        }, firstDelay, periodInMilliseconds, TimeUnit.MILLISECONDS);
    }

    public boolean getUsability(ObjectType target) {
        if (LIMIT < 0) return true;
        if (!usageRecord.containsKey(target)) usageRecord.put(target, 0L);
        return usageRecord.get(target) < LIMIT;
    }

    public void add(ObjectType target, long delta) {
        if (LIMIT < 0) return;
        if (!usageRecord.containsKey(target)) usageRecord.put(target, 0L);
        usageRecord.put(target, usageRecord.get(target) + delta);
    }

    public long getLIMIT() {
        return LIMIT;
    }

    public long getNextReset() {
        if (LIMIT < 0) return Long.MAX_VALUE;
        return nextReset;
    }
}
