package com.calo.cmpp;

import com.calo.cmpp.controller.MainWindow;
import com.zx.sms.connect.manager.EndpointManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CmppUiApplication {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(MainWindow::createWindow);
        SpringApplication.run(CmppUiApplication.class, args);
        MainWindow.showWindow();
        EndpointManager.INS.startConnectionCheckTask();
    }
}
