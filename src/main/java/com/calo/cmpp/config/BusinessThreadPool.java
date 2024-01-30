package com.calo.cmpp.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.Getter;

import java.util.concurrent.*;


public class BusinessThreadPool {

    @Getter
    private final static ExecutorService sendGroup;
    @Getter
    private final static ScheduledExecutorService busiGroup;

    static {
        sendGroup = new ThreadPoolExecutor(4, 16,
                100,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000),
                getThreadFactory("SEND-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        busiGroup = new ScheduledThreadPoolExecutor(4, getThreadFactory("BUSI-", true));
    }



    private static ThreadFactory getThreadFactory(String threadName, boolean isDaemon) {
        return new ThreadFactoryBuilder().setDaemon(isDaemon).setNamePrefix(threadName).build();
    }
}
