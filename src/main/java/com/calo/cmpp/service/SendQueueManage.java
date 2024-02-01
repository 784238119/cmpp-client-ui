package com.calo.cmpp.service;


import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.calo.cmpp.config.BusinessThreadPool;
import com.calo.cmpp.controller.LogMonitor;
import com.calo.cmpp.domain.CmppChannelAccount;
import com.calo.cmpp.domain.SendMessageGenerating;
import com.calo.cmpp.domain.SendMessageSubmit;
import com.google.common.collect.Queues;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.StandardCharsets;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class SendQueueManage {

    @Autowired
    private LogMonitor logMonitor;
    @Autowired
    private MonitorSendManage monitorSendManage;
    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);
    private static boolean runGenerateStatus = false;
    private static final Map<Integer, LinkedBlockingQueue<SendMessageSubmit>> queueMap = new ConcurrentHashMap<>();

    public synchronized void generateSendMessage(SendMessageGenerating sendMessageGenerating) {
        log.info("开始生成短信：{}", sendMessageGenerating);
        runGenerateStatus = sendMessageGenerating.isContinuousGeneration();
        LinkedBlockingQueue<SendMessageSubmit> queue = getSendMessageSubmits(sendMessageGenerating.getSendAccountId());
        do try {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < sendMessageGenerating.getSendSize(); i++) {
                SendMessageSubmit sendMessageSubmit = new SendMessageSubmit();
                sendMessageSubmit.setChannelId(sendMessageGenerating.getSendAccountId());
                sendMessageSubmit.setLocalMessageId(snowflake.nextIdStr());
                sendMessageSubmit.setMobile(StringUtils.isNotBlank(sendMessageGenerating.getMobile()) ? sendMessageGenerating.getMobile() : RandomPhoneNumber.generateMobile(sendMessageGenerating.getMobileType()));
                sendMessageSubmit.setExtend(sendMessageGenerating.getExtensionCode());
                sendMessageSubmit.setContent(MessageContentManage.getContentRandom(sendMessageGenerating.getContent(),sendMessageGenerating.isRandomContent()));
                sendMessageSubmit.setCount(MessageContentManage.count(sendMessageGenerating.getContent()));
                queue.offer(sendMessageSubmit);
                monitorSendManage.addMessage(sendMessageSubmit);
            }

            long sleepTime = sendMessageGenerating.getGenerationSpeedNum() - (System.currentTimeMillis() - startTime);
            if (sleepTime > 0) {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
        } catch (Exception e) {
            log.error("生成短信异常：{}", e.getMessage());
        } while (runGenerateStatus);
    }

    private LinkedBlockingQueue<SendMessageSubmit> getSendMessageSubmits(Integer sendAccountId) {
        LinkedBlockingQueue<SendMessageSubmit> queue = queueMap.computeIfAbsent(sendAccountId, k -> new LinkedBlockingQueue<>());
        initConsumer(sendAccountId);
        return queue;
    }


    @Async
    public void stopGenerateSendMessage() {
        logMonitor.appendLog("停止生成短信");
        runGenerateStatus = false;
        queueMap.forEach((k, v) -> v.clear());
    }

    public void initConsumer(Integer sendAccountId) {
        BusinessThreadPool.getSendGroup().execute(() -> getMessageToSend(sendAccountId));
    }

    private void getMessageToSend(Integer sendAccountId) {
        LinkedBlockingQueue<SendMessageSubmit> queue = queueMap.get(sendAccountId);
        while (true) try {
            long millis = System.currentTimeMillis();
            CmppChannelAccount channelAccount = ChannelAccountManage.getAccount(sendAccountId);
            if (channelAccount == null) {
                log.info("没有这个账号！！！！！");
                queueMap.remove(sendAccountId);
                return;
            }
            ArrayList<SendMessageSubmit> sendMessageSubmits = new ArrayList<>();
            Queues.drain(queue, sendMessageSubmits, channelAccount.getSpeed() * channelAccount.getMaxConnect(), 1000, TimeUnit.MILLISECONDS);
            sendMessageSubmits.forEach(messageSubmit -> this.submitMessageSend(channelAccount, messageSubmit));
            long sleepTime = 1000 - (System.currentTimeMillis() - millis);
            if (sleepTime > 0) {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
        } catch (Exception e) {
            log.info("取出数据异常：{}", e.getMessage());
        }
    }

    public void submitMessageSend(CmppChannelAccount cmppChannelAccount, SendMessageSubmit sendMessageSubmit) {
        try {
            CmppSubmitRequestMessage submitMessage = this.getCmppSubmitRequestMessage(cmppChannelAccount, sendMessageSubmit);
            EndpointConnector<?> managerEndpointConnector = EndpointManager.INS.getEndpointConnector(String.valueOf(cmppChannelAccount.getId()));
            if (managerEndpointConnector == null) {
                monitorSendManage.delFailureMsgId(1, sendMessageSubmit.getLocalMessageId());
                throw new RuntimeException("通道未连接，通道号：{}" + cmppChannelAccount.getId());
            }
            List<CmppSubmitRequestMessage> cmppSubmitRequestMessages = ChannelUtil.splitLongSmsMessage(managerEndpointConnector.getEndpointEntity(), submitMessage);
            for (CmppSubmitRequestMessage cmppSubmitRequestMessage : cmppSubmitRequestMessages) {
                if (managerEndpointConnector.getConnectionNum() == 0) {
                    monitorSendManage.delFailureMsgId(cmppSubmitRequestMessage.getSequenceNo(), sendMessageSubmit.getLocalMessageId());
                    logMonitor.appendLog("提交到网关失败，通道未连接：" + cmppSubmitRequestMessage);
                } else {
                    monitorSendManage.addSequence(cmppSubmitRequestMessage.getSequenceNo(), sendMessageSubmit.getLocalMessageId());
                    logMonitor.appendLog("提交到网关: " + cmppSubmitRequestMessage);
                    managerEndpointConnector.synwriteUncheck(cmppSubmitRequestMessage);
                }
            }
        } catch (Exception e) {
            logMonitor.appendLog("提交到网关异常:" + sendMessageSubmit);
        }
    }

    private CmppSubmitRequestMessage getCmppSubmitRequestMessage(CmppChannelAccount cmppChannelAccount, SendMessageSubmit sendMessageSubmit) {
        String accessCode = cmppChannelAccount.getSrcId() == null ? "" : cmppChannelAccount.getSrcId();
        String extend = sendMessageSubmit.getExtend() == null ? "" : sendMessageSubmit.getExtend();
        String srcId = StringUtils.substring(accessCode + extend, 0, 20);
        CmppSubmitRequestMessage message = CmppSubmitRequestMessage.create(sendMessageSubmit.getMobile(), srcId, new String(sendMessageSubmit.getContent().getBytes(), StandardCharsets.UTF_8));
        message.setRegisteredDelivery((short) 1);
        message.setMsgsrc(cmppChannelAccount.getUsername());
        return message;
    }


}
