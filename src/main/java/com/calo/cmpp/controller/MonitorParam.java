package com.calo.cmpp.controller;

import com.calo.cmpp.config.BusinessThreadPool;
import com.calo.cmpp.module.RoundedPanel;
import com.calo.cmpp.service.MonitorSendManage;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class MonitorParam extends RoundedPanel implements MonitorWin {

    private MonitorSendManage monitorSendManage;
    private final JLabel submitMessageCount = new JLabel("0");
    private final JLabel submitSuccessCount = new JLabel("0");
    private final JLabel submitFailureCount = new JLabel("0");
    private final JLabel responseCount = new JLabel("0");
    private final JLabel responseSuccessCount = new JLabel("0");
    private final JLabel reportCount = new JLabel("0");
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
        jPanelTop.add(setNumberFixedSize(new JLabel("生成数量:"), submitMessageCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("回执响应:"), responseCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("发送成功:"), sendSuccessCount));

        jPanelTop.add(setNumberFixedSize(new JLabel("发送速度:"), submissionSpeed));
        jPanelTop.add(setNumberFixedSize(new JLabel("提交成功:"), submitSuccessCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("响应成功:"), responseSuccessCount));

        jPanelTop.add(setNumberFixedSize(new JLabel("发送失败:"), sendFailureCount));
        jPanelTop.add(setNumberFixedSize(new JLabel("响应速度:"), responseSpeed));
        jPanelTop.add(setNumberFixedSize(new JLabel("提交失败:"), submitFailureCount));

        jPanelTop.add(setNumberFixedSize(new JLabel("回执报告:"), reportCount));
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

        // 添加右键菜单
        this.setComponentPopupMenu(createPopupMenu());

        BusinessThreadPool.getBusiGroup().scheduleAtFixedRate(this::refreshData, 0, 50, TimeUnit.MILLISECONDS);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem1 = new JMenuItem("清空数据");
        menuItem1.addActionListener(e -> {
            monitorSendManage.clearData();
            log.info("清空数据");
        });
        popupMenu.add(menuItem1);
        return popupMenu;
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
        SwingUtilities.invokeLater(() -> {
            int messageNum = monitorSendManage.getMessageCount().intValue();
            int submitsNum = monitorSendManage.getSubmitsCount().intValue();

            int submitSuccessNum = monitorSendManage.getResponseSuccessCount().intValue();
            int submitFailureNum = monitorSendManage.getResponseFailureCount().intValue();
            int responseNum = submitSuccessNum + submitFailureNum;

            int sendSuccessNum = monitorSendManage.getSendSuccessCount().intValue();
            int sendFailureNum = monitorSendManage.getSendFailureCount().intValue();
            int reportNum = sendSuccessNum + sendFailureNum;
            int sendUnknownNum = reportNum - (sendSuccessNum + sendFailureNum);

            // 保留两位小数
            String responseRate = (responseNum == 0 ? "00.00" : BigDecimal.valueOf((double) responseNum / submitsNum * 100.00).setScale(2, RoundingMode.HALF_UP).toString()) + "%";
            String reportingRate = (reportNum == 0 ? "00.00" : BigDecimal.valueOf((double) reportNum / submitSuccessNum * 100.00).setScale(2, RoundingMode.HALF_UP).toString()) + "%";
            String successRate = (sendSuccessNum == 0 ? "00.00" : BigDecimal.valueOf((double) sendSuccessNum / reportNum * 100.00).setScale(2, RoundingMode.HALF_UP).toString()) + "%";
            String failureRate = (sendFailureNum == 0 ? "00.00" : BigDecimal.valueOf((double) sendFailureNum / reportNum * 100.00).setScale(2, RoundingMode.HALF_UP).toString()) + "%";
            String unknownRate = (sendUnknownNum == 0 ? "00.00" : BigDecimal.valueOf((double) sendUnknownNum / reportNum * 100.00).setScale(2, RoundingMode.HALF_UP).toString()) + "%";

            this.submitMessageCount.setText(String.valueOf(messageNum));
            this.submitSuccessCount.setText(String.valueOf(submitsNum));
            this.submitFailureCount.setText(String.valueOf(submitFailureNum));
            this.responseCount.setText(String.valueOf(responseNum));
            this.responseSuccessCount.setText(String.valueOf(submitSuccessNum));
            this.reportCount.setText(String.valueOf(reportNum));
            this.sendSuccessCount.setText(String.valueOf(sendSuccessNum));
            this.sendFailureCount.setText(String.valueOf(sendFailureNum));
            this.sendUnknownCount.setText(String.valueOf(sendUnknownNum));

            this.submissionSpeed.setText(MonitorSendManage.getSubmitSpeed() + "/s");
            this.responseSpeed.setText(MonitorSendManage.getResponseSpeed() + "/s");
            this.reportSpeed.setText(MonitorSendManage.getReportSpeed() + "/s");

            this.responseRate.setText(responseRate);
            this.reportRate.setText(reportingRate);
            this.sendSuccessRate.setText(successRate);
            this.sendFailureRate.setText(failureRate);
            this.sendUnknownRate.setText(unknownRate);
        });
    }


    @Override
    public void refreshPageRendering() {
        this.refreshData();
    }

    @Autowired
    public void setMonitorSendManage(MonitorSendManage monitorSendManage) {
        this.monitorSendManage = monitorSendManage;
    }
}
