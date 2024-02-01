package com.calo.cmpp.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.calo.cmpp.domain.SendMessageSubmit;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

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
    public final LongAdder messageCount = new LongAdder();
    @Getter
    public final LongAdder submitsCount = new LongAdder();
    @Getter
    public final LongAdder responseSuccessCount = new LongAdder();
    @Getter
    public final LongAdder responseFailureCount = new LongAdder();
    @Getter
    public final LongAdder sendFailureCount = new LongAdder();
    @Getter
    public final LongAdder sendSuccessCount = new LongAdder();

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
    public void calculateNumberRecordSecondSpeed() {
        long submitsNewOffset = submitsCount.longValue();
        submitSpeed = submitsNewOffset - submitOffset;
        submitOffset = submitsNewOffset;

        long responseNewOffset = responseSuccessCount.longValue() + responseFailureCount.longValue();
        responseSpeed = responseNewOffset - responseOffset;
        responseOffset = responseNewOffset;

        long reportNewOffset = sendFailureCount.longValue() + sendSuccessCount.longValue();
        reportSpeed = reportNewOffset - reportOffset;
        reportOffset = reportNewOffset;
    }

    public void addMessage(SendMessageSubmit sendMessageSubmit) {
        messageCache.put(sendMessageSubmit.getLocalMessageId(), sendMessageSubmit);
        messageCount.add(sendMessageSubmit.getCount());
    }

    public void addSequence(int sequence, String localMessageId) {
        submitsCount.add(1);
        suiteCache.put(sequence, localMessageId);
    }

    public void addReport(String msgId, String statusCode) {
        String localMessageId = msgIdCache.get(msgId);
        if (localMessageId == null) {
            return;
        }
        msgIdCache.remove(msgId);
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if ("DELIVRD".equals(statusCode)) {
            sendSuccessCount.add(1);
        } else {
            sendFailureCount.add(1);
        }
        sendMessageSubmit.getStatus().add(statusCode);
        fifoCache.put(msgId, sendMessageSubmit);
    }

    public void addSuccessMsgId(int sequenceNo, String msgId) {
        responseSuccessCount.add(1);
        String localMessageId = suiteCache.get(sequenceNo);
        if (localMessageId == null) {
            return;
        }
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if (sendMessageSubmit != null) {
            sendMessageSubmit.getMsgId().add(msgId);
            msgIdCache.put(msgId,sendMessageSubmit.getLocalMessageId());
        }
    }

    public void delFailureMsgId(int sequenceNo, String msgId) {
        suiteCache.remove(sequenceNo);
        msgIdCache.remove(msgId);
        responseFailureCount.add(1);
    }

    public void clearData() {
        messageCount.reset();
        submitsCount.reset();
        responseSuccessCount.reset();
        responseFailureCount.reset();
        sendFailureCount.reset();
        sendSuccessCount.reset();
        submitSpeed = 0;
        submitOffset = 0;
        responseSpeed = 0;
        responseOffset = 0;
        reportSpeed = 0;
        reportOffset = 0;
        msgIdCache.clear();
        suiteCache.clear();
        fifoCache.clear();
        messageCache.clear();
    }
}
