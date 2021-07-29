package com.huawei.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolFactory.class);

    public static ExecutorService newBigThreadPool(String threadName) {
        return newBigThreadPool(threadName, 2000);
    }

    private static ExecutorService newBigThreadPool(String threadName, int size) {
        int threadNum = calculateOptimalThreadCount(1, 2, new BigDecimal("0.2"));
        int queueSize = size;
        int corePoolSize = calculateCorePoolSize(threadNum);
        LOGGER.info("Thread name is {}, corePoolSize is : {}, size is {}, queueSize is {}", threadName, corePoolSize,
                threadNum, queueSize);
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(queueSize);

        ThreadPoolExecutor bigThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, threadNum, 60L,
                TimeUnit.SECONDS, blockingQueue, new MigrationThreadFactory("migration", threadName, false),
                new DiscardOldestPolicy(LOGGER, threadName));
        bigThreadPoolExecutor.allowCoreThreadTimeOut(true);
        return bigThreadPoolExecutor;
    }

    private static int calculateCorePoolSize(int threadNum) {
        return new BigDecimal(threadNum).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_UP).intValue();
    }

    private static int calculateOptimalThreadCount(long cpu, long wait, BigDecimal targetUtilization) {
        BigDecimal waitTime = new BigDecimal(wait);
        BigDecimal computeTime = new BigDecimal(cpu);
        BigDecimal numberOfCPU = new BigDecimal(Runtime.getRuntime().availableProcessors());
        BigDecimal optimalThreadCount = numberOfCPU.multiply(targetUtilization)
                .multiply(new BigDecimal(1).add(waitTime.divide(computeTime, RoundingMode.HALF_UP)))
                .setScale(0, BigDecimal.ROUND_UP);
        return optimalThreadCount.intValue();
    }

    public static class MigrationThreadFactory implements ThreadFactory {
        private static final ConcurrentHashMap<String, ThreadGroup> THREAD_GROUPS = new ConcurrentHashMap<>();

        private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);

        private final AtomicLong counter = new AtomicLong(0L);

        private final int poolId;

        private final ThreadGroup group;

        private final String prefix;

        private final boolean daemon;

        public MigrationThreadFactory(String groupName, String prefix, boolean daemon) {
            this.poolId = POOL_COUNTER.incrementAndGet();
            this.prefix = prefix;
            this.daemon = daemon;
            this.group = this.initThreadGroup(groupName);
        }

        @Override
        public Thread newThread(Runnable r) {
            String trName = String.format("Pool-%d-%s-%d", this.poolId, this.prefix, this.counter.incrementAndGet());
            Thread thread = new Thread(this.group, r);
            thread.setName(trName);
            thread.setDaemon(this.daemon);
            return thread;
        }

        private ThreadGroup initThreadGroup(String groupName) {
            if (THREAD_GROUPS.get(groupName) == null) {
                THREAD_GROUPS.putIfAbsent(groupName, new ThreadGroup(groupName));
            }

            return THREAD_GROUPS.get(groupName);
        }
    }

}
