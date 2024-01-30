package com.calo.cmpp.controller;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Log4j2
@Component
public class MainMonitorWin extends JPanel implements MonitorWin {

    {
        this.setLayout(new GridBagLayout());
    }

    @Resource
    private AccountForm accountForm;

    @Resource
    private MonitorParam monitorParam;

    @PostConstruct
    public void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(monitorParam, BorderLayout.NORTH);
        this.add(accountForm, BorderLayout.CENTER);
    }

    @Override
    public void refreshPageRendering() {
        accountForm.refreshPageRendering();
        monitorParam.refreshPageRendering();
    }
}
