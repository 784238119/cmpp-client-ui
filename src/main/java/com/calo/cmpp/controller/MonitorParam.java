package com.calo.cmpp.controller;

import com.calo.cmpp.config.BusinessThreadPool;
import com.calo.cmpp.module.RoundedPanel;
import com.calo.cmpp.service.MonitorSendManage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class MonitorParam extends RoundedPanel implements MonitorWin {

    @Resource
    private MonitorSendManage monitorSendManage;
    private final JLabel submitMessageCount = new JLabel("0");
    private final JLabel submitSuccessCount = new JLabel("0");
    private final JLabel submitFailureCount = new JLabel("0");
    private final JLabel responseCount = new JLabel("0");
    private final JLabel reportCount = new JLabel("0");
    private final JLabel notReportCount = new JLabel("0");
    private final JLabel sendSuccessCount = new JLabel("0");
    private final JLabel sendFailureCount = new JLabel("0");
    private final JLabel sendUnknownCount = new JLabel("0");

    private final JLabel submissionSpeed = new JLabel("0/s");
    private final JLabel responseSpeed = new JLabel("0/s");
    private final JLabel reportSpeed = new JLabel("0/s");

    private final JLabel responseRate = new JLabel("00%");
    private final JLabel reportRate = new JLabel("00%");
    private final JLabel sendSuccessRate = new JLabel("00%");
    private final JLabel sendFailureRate = new JLabel("00%");
    private final JLabel sendUnknownRate = new JLabel("00%");

    {
        this.setBorder(new EmptyBorder(10, 100, 10, 30));
    }

    @PostConstruct
    private void init() {
        JPanel jPanelTop = new JPanel();
        jPanelTop.setLayout(new GridLayout(3, 2, 0, 0));
        jPanelTop.setMinimumSize(new Dimension(900, 150));
        jPanelTop.add(setNumberFixedSize(new JLabel("提交数量:"), submitMessageCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("回执响应:"), responseCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("发送成功:"), sendSuccessCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("发送速度:"), submissionSpeed));
        jPanelTop.add(setNumberFixedSize(new JLabel("提交成功:"), submitSuccessCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("回执报告:"), reportCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("发送失败:"), sendFailureCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("响应速度:"), responseSpeed));
        jPanelTop.add(setNumberFixedSize(new JLabel("提交失败:"), submitFailureCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("未知报告:"), notReportCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("发送未知:"), sendUnknownCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("报告速度:"), reportSpeed));
        this.add(jPanelTop);

        JPanel jPanelBot = new JPanel();
        jPanelBot.add(setNumberFixedSize(new JLabel("响应率:"), responseRate));
        jPanelBot.add(setNumberFixedSize(new JLabel("报告率:"), reportRate));
        jPanelBot.add(setNumberFixedSize(new JLabel("成功率:"), sendSuccessRate));
        jPanelBot.add(setNumberFixedSize(new JLabel("失败率:"), sendFailureRate));
        jPanelBot.add(setNumberFixedSize(new JLabel("未知率:"), sendUnknownRate));
        this.add(jPanelBot);

        BusinessThreadPool.getBusiGroup().scheduleAtFixedRate(this::refreshData, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private JPanel setNumberFixedSize(JLabel label, JLabel number) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        Dimension fixedSize = new Dimension(80, 20);
        number.setPreferredSize(fixedSize);
        number.setMinimumSize(fixedSize);
        number.setMaximumSize(fixedSize);
        number.setHorizontalAlignment(SwingConstants.LEFT);
        jPanel.add(label);
        jPanel.add(number);
        return jPanel;
    }

    public void refreshData() {
        int messageNum = monitorSendManage.getMessageCount().get();
        int submitsNum = monitorSendManage.getMessageCount().get();
        int answersNum = monitorSendManage.getAnswersCount().get();
        int successNum = monitorSendManage.getSuccessCount().get();
        int failureNum = monitorSendManage.getFailureCount().get();
        int mistakeNum = monitorSendManage.getMistakeCount().get();
        int sendsOKNum = monitorSendManage.getSendsOKCount().get();

        String responseRate = (answersNum == 0 ? "00" : Math.round((double) answersNum / submitsNum * 100.0)) + "%";
        String reportingRate = (failureNum + successNum == 0 ? "00" : Math.round((double) (successNum + failureNum) / (double) submitsNum * 100.0)) + "%";
        String successRate = (successNum == 0 ? "00" : Math.round((double) successNum / answersNum * 100.0)) + "%";
        String failureRate = (failureNum == 0 ? "00" : Math.round((double) failureNum / answersNum * 100.0)) + "%";
        String unknownRate = (answersNum - successNum - failureNum == 0 ? "00" : Math.round((double) (answersNum - failureNum - successNum) / answersNum * 100.0)) + "%";

        this.submitMessageCount.setText(String.valueOf(messageNum));
        this.submitSuccessCount.setText(String.valueOf(submitsNum));
        this.submitFailureCount.setText(String.valueOf(mistakeNum));
        this.responseCount.setText(String.valueOf(answersNum));
        this.reportCount.setText(String.valueOf(successNum + failureNum));
        this.notReportCount.setText(String.valueOf((answersNum - failureNum - successNum)));
        this.sendSuccessCount.setText(String.valueOf(sendsOKNum));
        this.sendFailureCount.setText(String.valueOf(failureNum));
        this.sendUnknownCount.setText(String.valueOf(answersNum - failureNum - successNum));

        this.submissionSpeed.setText(MonitorSendManage.getSubmitSpeed() + "/s");
        this.responseSpeed.setText(MonitorSendManage.getResponseSpeed() + "/s");
        this.reportSpeed.setText(MonitorSendManage.getReportSpeed() + "/s");

        this.responseRate.setText(responseRate);
        this.reportRate.setText(reportingRate);
        this.sendSuccessRate.setText(successRate);
        this.sendFailureRate.setText(failureRate);
        this.sendUnknownRate.setText(unknownRate);
    }


    @Override
    public void refreshPageRendering() {
        this.refreshData();
    }
}
