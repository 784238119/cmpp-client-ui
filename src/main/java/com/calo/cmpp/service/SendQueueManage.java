package com.calo.cmpp.service;


import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.log.Log;
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
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Log4j2
public class SendQueueManage {

    @Resource
    private LogMonitor logMonitor;
    @Resource
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
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        executorService.execute(() -> getMessageToSend(sendAccountId));
    }

    private void getMessageToSend(Integer sendAccountId) {
        LinkedBlockingQueue<SendMessageSubmit> queue = queueMap.get(sendAccountId);
        while (true) try {
            CmppChannelAccount channelAccount = ChannelAccountManage.getAccount(sendAccountId);
            if (channelAccount == null) {
                log.info("没有这个账号！！！！！");
                queueMap.remove(sendAccountId);
                return;
            }
            ArrayList<SendMessageSubmit> sendMessageSubmits = new ArrayList<>();
            Queues.drain(queue, sendMessageSubmits, channelAccount.getSpeed(), 1, TimeUnit.SECONDS);
            sendMessageSubmits.forEach(messageSubmit -> this.submitMessageSend(channelAccount, messageSubmit));
        } catch (Exception e) {
            log.info("取出数据异常：{}", e.getMessage());
        }
    }

    public void submitMessageSend(CmppChannelAccount cmppChannelAccount, SendMessageSubmit sendMessageSubmit) {
        try {
            CmppSubmitRequestMessage submitMessage = this.getCmppSubmitRequestMessage(cmppChannelAccount, sendMessageSubmit);
            EndpointConnector<?> managerEndpointConnector = EndpointManager.INS.getEndpointConnector(String.valueOf(cmppChannelAccount.getId()));
            if (managerEndpointConnector == null) {
                monitorSendManage.delMsgId(1);
                throw new RuntimeException("通道未连接，通道号：{}" + cmppChannelAccount.getId());
            }
            List<CmppSubmitRequestMessage> cmppSubmitRequestMessages = ChannelUtil.splitLongSmsMessage(managerEndpointConnector.getEndpointEntity(), submitMessage);
            for (CmppSubmitRequestMessage cmppSubmitRequestMessage : cmppSubmitRequestMessages) {
                if (managerEndpointConnector.getConnectionNum() == 0) {
                    monitorSendManage.delMsgId(cmppSubmitRequestMessage.getSequenceNo());
                    logMonitor.appendLog("提交失败短信，通道未连接：" + cmppSubmitRequestMessage);
                } else {
                    monitorSendManage.addSequence(cmppSubmitRequestMessage.getSequenceNo(), sendMessageSubmit.getLocalMessageId());
                    logMonitor.appendLog("提交发送短信：" + cmppSubmitRequestMessage);
                    managerEndpointConnector.synwriteUncheck(cmppSubmitRequestMessage);
                }
            }
        } catch (Exception e) {
            logMonitor.appendLog("短信提交失败:" + sendMessageSubmit);
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
