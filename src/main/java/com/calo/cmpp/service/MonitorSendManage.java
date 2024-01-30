package com.calo.cmpp.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.calo.cmpp.domain.SendMessageSubmit;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MonitorSendManage {

    private static final int EXPIRATION = 3 * 24 * 60 * 60 * 1000;

    private static final TimedCache<String, String> msgIdCache = CacheUtil.newTimedCache(EXPIRATION);
    private static final TimedCache<Integer, String> suiteCache = CacheUtil.newTimedCache(EXPIRATION);

    private static final Cache<String, SendMessageSubmit> fifoCache = CacheUtil.newFIFOCache(100000000);
    private static final Cache<String, SendMessageSubmit> messageCache = CacheUtil.newTimedCache(EXPIRATION);
    @Getter
    private volatile static long submitSpeed = 0;
    private volatile static long submitOffset = 0;

    @Getter
    private volatile static long responseSpeed = 0;
    private volatile static long responseOffset = 0;

    @Getter
    private volatile static long reportSpeed = 0;
    private volatile static long reportOffset = 0;
    @Getter
    public final AtomicInteger messageCount = new AtomicInteger(0);
    @Getter
    public final AtomicInteger submitsCount = new AtomicInteger(0);
    @Getter
    public final AtomicInteger failureCount = new AtomicInteger(0);
    @Getter
    public final AtomicInteger successCount = new AtomicInteger(0);
    @Getter
    public final AtomicInteger answersCount = new AtomicInteger(0);
    @Getter
    public final AtomicInteger mistakeCount = new AtomicInteger(0);
    @Getter
    public final AtomicInteger sendsOKCount = new AtomicInteger(0);

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
    public void calculateNumberRecordSecondSpeed() {
        long submitsNewOffset = submitsCount.get();
        submitSpeed = submitsNewOffset - submitOffset;
        submitOffset = submitsNewOffset;

        long responseNewOffset = answersCount.get();
        responseSpeed = responseNewOffset - responseOffset;
        responseOffset = responseNewOffset;

        long reportNewOffset = failureCount.get() + successCount.get();
        reportSpeed = reportNewOffset - reportOffset;
        reportOffset = reportNewOffset;
    }

    public void addMessage(SendMessageSubmit sendMessageSubmit) {
        messageCache.put(sendMessageSubmit.getLocalMessageId(), sendMessageSubmit);
        messageCount.getAndAdd(sendMessageSubmit.getCount());
    }

    public void addSequence(int sequence, String localMessageId) {
        submitsCount.incrementAndGet();
        suiteCache.put(sequence, localMessageId);
    }

    public void addMsgId(int sequence, String msgId) {
        String localMessageId = suiteCache.get(sequence);
        if (localMessageId == null) {
            return;
        }
        suiteCache.remove(sequence);
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if (sendMessageSubmit != null) {
            sendMessageSubmit.getMsgId().add(msgId);
        }
        answersCount.incrementAndGet();
        sendsOKCount.incrementAndGet();
        msgIdCache.put(msgId, localMessageId);
    }

    public void delMsgId(int sequence) {
        String localMessageId = suiteCache.get(sequence);
        if (localMessageId == null) {
            return;
        }
        suiteCache.remove(sequence);
        mistakeCount.incrementAndGet();
    }

    public void delMsgId(int sequence, String msgId) {
        String localMessageId = suiteCache.get(sequence);
        if (localMessageId == null) {
            return;
        }
        suiteCache.remove(sequence);
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if (sendMessageSubmit != null) {
            sendMessageSubmit.getMsgId().add(msgId);
        }
        answersCount.incrementAndGet();
        mistakeCount.incrementAndGet();
    }

    public void addReport(String msgId, String statusCode) {
        String localMessageId = msgIdCache.get(msgId);
        if (localMessageId == null) {
            return;
        }
        msgIdCache.remove(msgId);
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if ("DELIVRD".equals(statusCode)) {
            successCount.getAndIncrement();
        } else {
            failureCount.getAndIncrement();
        }
        sendMessageSubmit.getStatus().add(statusCode);
        fifoCache.put(msgId, sendMessageSubmit);
    }
}
