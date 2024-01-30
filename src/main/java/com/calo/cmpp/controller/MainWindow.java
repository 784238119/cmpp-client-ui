package com.calo.cmpp.controller;


import com.calo.cmpp.config.Properties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Log4j2
@Component
public class MainWindow {

    private Properties properties;

    @Getter
    private static JFrame jframe;
    @Resource
    private MainTabLabel mainTabLabel;

    public static void createWindow() {
        if (GraphicsEnvironment.isHeadless()) {
            log.error("无法创建窗口");
            return;
        }
        JFrame.setDefaultLookAndFeelDecorated(true);

        jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(new Dimension(1100, 700));
        jframe.setMinimumSize(new Dimension(1100, 700));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - jframe.getWidth()) / 2;
        int y = (screenSize.height - jframe.getHeight()) / 2;
        jframe.setLocation(x, y);
        jframe.setIconImage(loadIconImage());
    }

    @PostConstruct
    public void init() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        jframe.add(mainTabLabel);
        log.info("软件名称：{}", properties.getName());
        log.info("软件版本：{}", properties.getVersion());
        log.info("操作系统：{}", System.getProperty("os.name"));
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    public static void showWindow() {
        jframe.setVisible(true);
    }

    private static Image loadIconImage() {
        ClassLoader classLoader = MainWindow.class.getClassLoader();
        URL imageUrl = classLoader.getResource("resources/images/icon.png");
        if (imageUrl == null) {
            return null;
        }
        return new ImageIcon(imageUrl).getImage();
    }

    @Autowired
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
