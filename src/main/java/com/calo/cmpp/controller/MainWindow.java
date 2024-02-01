package com.calo.cmpp.controller;


import com.calo.cmpp.config.Properties;
import com.formdev.flatlaf.FlatLightLaf;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

@Log4j2
@Component
public class MainWindow {

    private Properties properties;

    @Getter
    private static JFrame jframe;

    private MainTabLabel mainTabLabel;

    public static void createWindow() {
        if (GraphicsEnvironment.isHeadless()) {
            log.error("无法创建窗口");
            return;
        }
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setRoundedUI();
        JFrame.setDefaultLookAndFeelDecorated(true);

        jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(new Dimension(1100, 700));
        jframe.setMinimumSize(new Dimension(1100, 700));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - jframe.getWidth()) / 2;
        int y = (screenSize.height - jframe.getHeight()) / 2;
        jframe.setLocation(x, y);
        jframe.setTitle("CMPP客户端");
        jframe.setIconImage(loadIconImage());

    }

    @PostConstruct
    public void init() {
        jframe.add(mainTabLabel);
        log.info("软件名称：{}", properties.getName());
        log.info("软件版本：{}", properties.getVersion());
        log.info("操作系统：{}", System.getProperty("os.name"));
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

    // 设置全局的圆角边框
    private static void setRoundedUI() {
        UIManager.put("TextField.border", new RoundedBorder());
        UIManager.put("ComboBox.border", new RoundedBorder());
        UIManager.put("TextArea.border", new RoundedBorder());
    }

    // 自定义圆角边框
    private static class RoundedBorder implements Border {
        private final int radius;

        RoundedBorder() {
            this.radius = 5; // 设置圆角半径
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();

            // 设置抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D roundRect = new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
            g2d.draw(roundRect);

            g2d.dispose();        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(radius + 1, radius + 1, radius + 1, radius + 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    @Autowired
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Autowired
    public void setMainTabLabel(MainTabLabel mainTabLabel) {
        this.mainTabLabel = mainTabLabel;
    }
}
