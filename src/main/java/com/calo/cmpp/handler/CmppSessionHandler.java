package com.calo.cmpp.handler;

import com.calo.cmpp.controller.LogMonitor;
import com.calo.cmpp.service.MonitorSendManage;
import com.zx.sms.codec.cmpp.msg.CmppConnectResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CmppSessionHandler extends AbstractBusinessHandler {

    @Resource
    private LogMonitor logMonitor;
    private MonitorSendManage monitorSendManage;

    @Override
    public String name() {
        return "CmppSessionHandler";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof CmppSubmitResponseMessage message) {
            if (message.getResult() == 0) {
                monitorSendManage.addMsgId(message.getSequenceNo(), message.getMsgId().toString());
            } else {
                monitorSendManage.delMsgId(message.getSequenceNo(), message.getMsgId().toString());
            }
            logMonitor.appendLog("接受到响应: " + message);
            return;
        }

        if (msg instanceof CmppDeliverRequestMessage message && message.isReport()) {
            CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(message.getHeader().getSequenceId());
            responseMessage.setResult(0);
            responseMessage.setMsgId(message.getMsgId());
            ctx.channel().writeAndFlush(responseMessage);
            monitorSendManage.addReport(message.getReportRequestMessage().getMsgId().toString(), message.getReportRequestMessage().getStat());
            logMonitor.appendLog("接受到回执: " + message);
            return;
        }

        if (msg instanceof CmppDeliverRequestMessage message && !message.isReport()) {
            CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(message.getHeader().getSequenceId());
            responseMessage.setResult(0);
            responseMessage.setMsgId(message.getMsgId());
            ctx.channel().writeAndFlush(responseMessage);
            logMonitor.appendLog("接受到上行: " + message);
            return;
        }

        if (msg instanceof CmppConnectResponseMessage message) {
            long status = message.getStatus();
            if (status != 0) {
                logMonitor.appendLog("连接登录失败: status=" + status);
            }else {
                logMonitor.appendLog("连接登录成功: status=" + status);
            }
            return;
        }

        ctx.fireChannelRead(msg);
    }

    @Autowired
    public void setSendMessageSubmitManage(MonitorSendManage monitorSendManage) {
        this.monitorSendManage = monitorSendManage;
    }
}
