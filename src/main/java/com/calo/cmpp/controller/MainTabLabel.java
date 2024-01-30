package com.calo.cmpp.controller;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.swing.*;


@Log4j2
@Component
public class MainTabLabel extends JTabbedPane {

    @Resource
    private MainMonitorWin mainMonitor;
    @Resource
    private SendParam sendParam;
    @Resource
    private LogMonitor logMonitor;
    {
        this.setTabPlacement(JTabbedPane.TOP);
        this.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
    }

    @PostConstruct
    public void init() {
        //添加标签页
        this.addTab("监控面板", mainMonitor);
        this.addTab("发送参数", sendParam);
        this.addTab("发送日志", logMonitor);
        this.setSelectedIndex(2);
        // 切换标签页监听
        this.addChangeListener(e -> {
            if (this.getSelectedIndex() == 0) {
                mainMonitor.refreshPageRendering();
            }
            if (this.getSelectedIndex() == 1) {
                sendParam.initializeAccount();
            }
            if (this.getSelectedIndex() == 2) {
                logMonitor.refreshPageRendering();
            }
        });
    }

}
