package com.calo.cmpp.controller;


import com.calo.cmpp.config.BusinessThreadPool;
import com.calo.cmpp.domain.CmppChannelAccount;
import com.calo.cmpp.domain.SendMessageGenerating;
import com.calo.cmpp.enums.MobileType;
import com.calo.cmpp.module.NumberTextField;
import com.calo.cmpp.module.RoundedPanel;
import com.calo.cmpp.service.ChannelAccountManage;
import com.calo.cmpp.service.SendQueueManage;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Objects;

@Log4j2
@Component
public class SendParam extends RoundedPanel {

    @Autowired
    private SendQueueManage sendQueueManage;

    {
        this.setBorder(new EmptyBorder(10, 30, 10, 30));
    }

    private Integer sendAccountId;
    private String mobile = "13800138000";
    private MobileType mobileType = MobileType.RAND;
    private String content = "【签名】短信内容";
    private boolean isRandomContent = false;
    private String extensionCode = "";
    private Integer sendSize = 1;
    private Integer generationSpeedNum = 1000;
    private boolean continuousGeneration = false;

    private final JComboBox<CmppChannelAccount> comboBox = new JComboBox<>();
    private final JTextField mobileTextField = new JTextField();
    private final JComboBox<MobileType> mobileTypeComboBox = new JComboBox<>();
    private final JTextArea contentTextArea = new JTextArea();
    private final JCheckBox randomContentCheckBox = new JCheckBox("随机内容");
    private final JTextField extensionCodeTextField = new JTextField();
    private final JTextField sendNumberSizeField = new JTextField();
    private final JTextField generationSpeed = new JTextField();
    private final JCheckBox continuousGenerationCheckBox = new JCheckBox("持续生成");


    @PostConstruct
    public void init() {
        setAccountSelection();
        this.add(setNumberFixedSize(new JLabel("发送账号："), comboBox, 700, 30));

        JPanel jPanel1 = new JPanel(new FlowLayout());
        setCarrier();
        setPhoneNumber();
        jPanel1.add(setNumberFixedSize(new JLabel("手机号码："), mobileTextField, 400, 30));
        jPanel1.add(setNumberFixedSize(new JLabel("运营商："), mobileTypeComboBox, 180, 30));
        this.add(jPanel1);

        JPanel jPanel2 = new JPanel(new FlowLayout());
        JScrollPane scrollPane = setSMSContent();
        // 随机内容
        setRandomContent();
        setExtensionCode();
        jPanel2.add(setNumberFixedSize(new JLabel("短信内容："), scrollPane, 620, 350));
        jPanel2.add(randomContentCheckBox);
        this.add(jPanel2);

        this.add(setNumberFixedSize(new JLabel("扩展码号："), extensionCodeTextField, 700, 30));

        JPanel jPanel3 = new JPanel(new FlowLayout());
        setSendSize();
        setGenerationSpeed();
        setContinuousGeneration();
        jPanel3.add(setNumberFixedSize(new JLabel("发送数量："), sendNumberSizeField, 170, 30));
        jPanel3.add(setNumberFixedSize(new JLabel("生成速度："), generationSpeed, 170, 30));
        jPanel3.add(continuousGenerationCheckBox);

        JButton startButton = new JButton("发送");
        JButton stopjButton = new JButton("停止");
        setStartButton(startButton);
        setStopJButton(stopjButton);
        jPanel3.add(startButton);
        jPanel3.add(stopjButton);

        this.add(jPanel3);
    }

    private void setStopJButton(JButton stopjButton) {
        stopjButton.addActionListener(e -> sendQueueManage.stopGenerateSendMessage());
    }

    private void setStartButton(JButton startButton) {
        startButton.addActionListener(e -> {
            if (sendAccountId == null || ChannelAccountManage.getAccount(sendAccountId) == null) {
                JOptionPane.showMessageDialog(null, "请选择发送账号", "提示", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (StringUtils.isBlank(content) && !isRandomContent) {
                JOptionPane.showMessageDialog(null, "请输入短信内容", "提示", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (sendSize == null || sendSize <= 0) {
                JOptionPane.showMessageDialog(null, "请输入发送数量", "提示", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (generationSpeedNum == null || generationSpeedNum <= 0) {
                JOptionPane.showMessageDialog(null, "请输入生成速度", "提示", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BusinessThreadPool.getBusiGroup().execute(() -> sendQueueManage.generateSendMessage(new SendMessageGenerating(sendAccountId, mobile, mobileType, content, isRandomContent, extensionCode, continuousGeneration, sendSize, generationSpeedNum)));
        });
    }

    private void setContinuousGeneration() {
        continuousGenerationCheckBox.setSelected(false);
        continuousGenerationCheckBox.setMinimumSize(new Dimension(40, 40));
        continuousGenerationCheckBox.addItemListener(e -> {
            continuousGeneration = e.getStateChange() == ItemEvent.SELECTED;
        });
        sendNumberSizeField.setToolTipText("持续生成消息配合生成速度一起工作");
    }

    private void setGenerationSpeed() {
        generationSpeed.setDocument(new NumberTextField());
        generationSpeed.setText(String.valueOf(generationSpeedNum));
        // 光标消失监听
        generationSpeed.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (StringUtils.isNotBlank(generationSpeed.getText())) {
                    generationSpeedNum = Integer.parseInt(generationSpeed.getText());
                } else {
                    generationSpeedNum = 0;
                }
            }
        });
        generationSpeed.setToolTipText("生成速度，单位毫秒");
    }

    private void setSendSize() {
        sendNumberSizeField.setDocument(new NumberTextField());
        sendNumberSizeField.setText(String.valueOf(sendSize));
        // 光标消失监听
        sendNumberSizeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                String text = sendNumberSizeField.getText();
                if (StringUtils.isNotBlank(text)) {
                    sendSize = Integer.parseInt(text);
                } else {
                    sendSize = 0;
                }
            }
        });
        sendNumberSizeField.setToolTipText("请输入一个批次发送数量");
    }

    private void setExtensionCode() {
        extensionCodeTextField.setDocument(new NumberTextField());
        extensionCodeTextField.setText(extensionCode);
        // 光标消失监听
        extensionCodeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                extensionCode = extensionCodeTextField.getText();
            }
        });
        sendNumberSizeField.setToolTipText("发送短信的扩展码号");
    }

    private void setRandomContent() {
        randomContentCheckBox.setSelected(isRandomContent);
        randomContentCheckBox.setMinimumSize(new Dimension(50, 40));
        randomContentCheckBox.addItemListener(e -> {
            isRandomContent = e.getStateChange() == ItemEvent.SELECTED;
        });
        sendNumberSizeField.setToolTipText("随机内容，每次发送内容不一样(和内容文本框互斥)");
    }

    private JScrollPane setSMSContent() {
        // 短信内容
        contentTextArea.setLineWrap(true);
        contentTextArea.setWrapStyleWord(true);
        contentTextArea.setText(content);

        // 滚动条
        JScrollPane scrollPane = new JScrollPane(contentTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(238, 238, 238)));

        // 光标消失监听
        contentTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                content = contentTextArea.getText();
            }
        });
        return scrollPane;
    }

    private void setCarrier() {
        for (MobileType type : MobileType.values()) {
            mobileTypeComboBox.addItem(type);
        }
        mobileTypeComboBox.setSelectedItem(mobileType);
        // 选择账号监听
        mobileTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                mobileType = (MobileType) e.getItem();
            }
        });

    }

    private void setPhoneNumber() {
        mobileTextField.setDocument(new NumberTextField());
        mobileTextField.setText(mobile);
        // 光标消失监听
        mobileTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                mobile = mobileTextField.getText();
            }
        });
        mobileTextField.setToolTipText("请输入手机号码");
    }

    private void setAccountSelection() {
        initializeAccount();
        // 选择账号监听
        if (sendAccountId != null) {
            comboBox.setSelectedIndex(-1);
        }
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                CmppChannelAccount account = (CmppChannelAccount) e.getItem();
                sendAccountId = account.getId();
            }
        });

    }

    public void initializeAccount() {
        comboBox.removeAllItems();
        ChannelAccountManage.getAccountAll().forEach(comboBox::addItem);
    }

    private static JPanel setNumberFixedSize(JLabel label, JComponent component, int width, int height) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        Dimension fixedSize = new Dimension(width, height);
        component.setPreferredSize(fixedSize);
        component.setMinimumSize(fixedSize);
        component.setMaximumSize(fixedSize);
        jPanel.add(label);
        jPanel.add(component);
        return jPanel;
    }


}
