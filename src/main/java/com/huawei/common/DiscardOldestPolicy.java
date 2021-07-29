package com.huawei.common;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

/**
 * 重载默认丢弃策略，用于丢弃时打印日志
 */
public class DiscardOldestPolicy extends ThreadPoolExecutor.DiscardOldestPolicy {
    private AtomicLong discard = new AtomicLong(0);

    private Logger logger;

    private String threadName = "";

    public DiscardOldestPolicy(Logger logger) {
        this.logger = logger;
    }

    public DiscardOldestPolicy(Logger logger, String threadName) {
        this.logger = logger;
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        super.rejectedExecution(r, e);
        final long rejectedSum = discard.incrementAndGet();
        if (rejectedSum == 1 || rejectedSum % 100 == 0) {
            logger.error(
                    "DiscardOldest worker, had discard {}, taskCount {}, completedTaskCount {}, largestPoolSize {}," +
                    "getPoolSize {}, getActiveCount {}, getThreadName {}",
                    rejectedSum, e.getTaskCount(), e.getCompletedTaskCount(), e.getLargestPoolSize(),
                    e.getPoolSize(), e.getActiveCount(), threadName);
        }
    }
}
