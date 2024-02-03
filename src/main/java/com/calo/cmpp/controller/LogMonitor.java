package com.calo.cmpp.controller;


import cn.hutool.core.date.DateTime;
import com.calo.cmpp.module.RoundedPanel;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
@Component
public class LogMonitor extends RoundedPanel implements MonitorWin {

    private final JTextArea logTextArea = new JTextArea();
    private final JScrollPane logScrollPane = new JScrollPane(logTextArea);
    // 链表，用于存储日志
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(1000);

    {
        this.setBorder(new EmptyBorder(5, 5, 5, 0));
        this.add(logScrollPane);
        this.add(logScrollPane, BorderLayout.CENTER);
    }

    @PostConstruct
    public void init() {
        logTextArea.setEditable(false);
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logTextArea.setBackground(new Color(242, 242, 242));
        // 显示最下面
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        // 设置光标颜色
        logTextArea.setCaretColor(new Color(242, 242, 242));
        // 去除边框
        logScrollPane.setBorder(BorderFactory.createLineBorder(new Color(242, 242, 242)));
        logTextArea.setBorder(BorderFactory.createLineBorder(new Color(242, 242, 242)));
        // 滚动时才显示滚动条
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // 设置首选大小，以及文本区域的行数
        logScrollPane.setViewportView(logTextArea);
        // 设置滚动面板的首选大小
        logScrollPane.setPreferredSize(new Dimension(1050, 590));
        setRightClickMenu();
    }

    private void setRightClickMenu() {
        // 设置右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem clearItem1 = new JMenuItem("清空日志");
        JMenuItem clearItem2 = new JMenuItem("设置换行");

        clearItem1.addActionListener(e -> {
            logQueue.clear();
            logTextArea.setText("");
        });
        clearItem2.addActionListener(e -> {
            if (logTextArea.getLineWrap()) {
                logTextArea.setLineWrap(false);
                logTextArea.setWrapStyleWord(false);
            } else {
                logTextArea.setLineWrap(true);
                logTextArea.setWrapStyleWord(true);
            }
        });
        popupMenu.add(clearItem1);
        popupMenu.add(clearItem2);
        logTextArea.setComponentPopupMenu(popupMenu);
    }

    public void appendLog(String log) {
        String logText = "[" + DateTime.now().toString("yyyy-MM-dd HH:mm:ss") + "] " + log + "\n";
        // 如果队列已满，移除头部元素
        if (!logQueue.offer(logText)) {
            logQueue.poll();
            logQueue.offer(logText);
        }
    }

    @Scheduled(fixedDelay = 500)
    public void changeLog() throws InterruptedException {
        SwingUtilities.invokeLater(() -> {
            StringBuilder logText = new StringBuilder();
            logQueue.forEach(logText::append);
            logTextArea.setText(logText.toString());
            // 显示最下面
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }


    @Override
    public void refreshPageRendering() {
        try {
            changeLog();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
