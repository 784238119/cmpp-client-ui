package com.calo.cmpp;

import com.calo.cmpp.controller.MainWindow;
import com.zx.sms.connect.manager.EndpointManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CmppUiApplication {

    public static void main(String[] args) throws InterruptedException {
        javax.swing.SwingUtilities.invokeLater(MainWindow::createWindow);
        TimeUnit.MILLISECONDS.sleep(500);
        SpringApplication.run(CmppUiApplication.class, args);
    }

    @Order(0)
    @PostConstruct
    public void init() {
        EndpointManager.INS.startConnectionCheckTask();
    }

    // 关闭所有连接
    @PreDestroy
    public void destroy() {
        EndpointManager.INS.stopConnectionCheckTask();
        EndpointManager.INS.removeAll();
    }

}
