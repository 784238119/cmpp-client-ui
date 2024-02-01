package com.calo.cmpp.module;

import com.calo.cmpp.domain.CmppAccountTableModel;
import com.calo.cmpp.domain.CmppChannelAccount;
import com.calo.cmpp.enums.Version;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

@Log4j2
public class AccountFormDialog {

    private final JDialog dialog = new JDialog();

    @Getter
    private CmppChannelAccount cmppChannelAccount;

    public AccountFormDialog(CmppChannelAccount account, CmppAccountTableModel cmppAccountTableModel) {
        this.cmppChannelAccount = account;
        if (cmppChannelAccount.getUsername() != null) {
            dialog.setTitle("编辑账号");
        } else {
            dialog.setTitle("新增账号");
        }
        dialog.setModal(true);
        // 设置在最前面
        dialog.setAlwaysOnTop(true);
        // 设置跟随的窗口
        dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        dialog.setSize(400, 450);
        dialog.setMinimumSize(new Dimension(400, 450));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 5, 5);

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, nameField.getPreferredSize().height));
        nameField.setText(cmppChannelAccount.getChannelName());

        JTextField hostField = new JTextField();
        hostField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));
        hostField.setText(cmppChannelAccount.getHost());

        JTextField portField = new JTextField();
        portField.setDocument(new NumberTextField());
        portField.setText(cmppChannelAccount.getPort());
        portField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));

        JComboBox<Version> versionField = new JComboBox<>(new Version[]{Version.CMPP30, Version.CMPP20});
        versionField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));
        versionField.setSelectedItem(Objects.requireNonNullElse(cmppChannelAccount.getVersion(), Version.CMPP30));

        JTextField usernameField = new JTextField(cmppChannelAccount.getUsername());
        usernameField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));
        usernameField.setText(cmppChannelAccount.getUsername());

        JTextField passwordField = new JTextField(cmppChannelAccount.getPassword());
        passwordField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));

        JTextField srcIdField = new JTextField();
        srcIdField.setDocument(new NumberTextField());
        srcIdField.setText(cmppChannelAccount.getSrcId());
        srcIdField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));

        JTextField speedField = new JTextField();
        speedField.setDocument(new NumberTextField());
        speedField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));
        speedField.setText(String.valueOf(cmppChannelAccount.getSpeed() == null ? 200 : cmppChannelAccount.getSpeed()));

        JTextField maxConnectField = new JTextField();
        maxConnectField.setDocument(new NumberTextField());
        maxConnectField.setPreferredSize(new Dimension(200, hostField.getPreferredSize().height));
        maxConnectField.setText(String.valueOf(cmppChannelAccount.getMaxConnect() == null ? 2 : cmppChannelAccount.getMaxConnect()));


        addFormField(formPanel, gbc, new JLabel("名称:"), 0, 0);
        addFormField(formPanel, gbc, nameField, 1, 0);
        addFormField(formPanel, gbc, new JLabel("地址:"), 0, 1);
        addFormField(formPanel, gbc, hostField, 1, 1);
        addFormField(formPanel, gbc, new JLabel("端口:"), 0, 2);
        addFormField(formPanel, gbc, portField, 1, 2);
        addFormField(formPanel, gbc, new JLabel("协议:"), 0, 3);
        addFormField(formPanel, gbc, versionField, 1, 3);
        addFormField(formPanel, gbc, new JLabel("账号:"), 0, 4);
        addFormField(formPanel, gbc, usernameField, 1, 4);
        addFormField(formPanel, gbc, new JLabel("密码:"), 0, 5);
        addFormField(formPanel, gbc, passwordField, 1, 5);
        addFormField(formPanel, gbc, new JLabel("码号:"), 0, 6);
        addFormField(formPanel, gbc, srcIdField, 1, 6);
        addFormField(formPanel, gbc, new JLabel("速度:"), 0, 7);
        addFormField(formPanel, gbc, speedField, 1, 7);
        addFormField(formPanel, gbc, new JLabel("连接:"), 0, 8);
        addFormField(formPanel, gbc, maxConnectField, 1, 8);

        JButton confirmButton = new JButton("确定");

        confirmButton.addActionListener(e -> {
            if (validateFields(nameField, hostField, portField, usernameField, passwordField, srcIdField, speedField, maxConnectField)) {
                cmppChannelAccount.setChannelName(nameField.getText());
                cmppChannelAccount.setHost(hostField.getText());
                cmppChannelAccount.setPort(portField.getText());
                cmppChannelAccount.setVersion((Version) versionField.getSelectedItem());
                cmppChannelAccount.setUsername(usernameField.getText());
                cmppChannelAccount.setPassword(passwordField.getText());
                cmppChannelAccount.setSrcId(srcIdField.getText());
                cmppChannelAccount.setSpeed(Integer.valueOf(speedField.getText()));
                cmppChannelAccount.setMaxConnect(Integer.valueOf(maxConnectField.getText()));
                cmppAccountTableModel.addAccount(cmppChannelAccount);
                log.info("账号：{}", cmppChannelAccount);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请填写所有必填字段", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(confirmButton, BorderLayout.SOUTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private static void addFormField(JPanel panel, GridBagConstraints gbc, JComponent component, int gridx, int gridy) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        panel.add(component, gbc);
    }

    private static boolean validateFields(JTextField... fields) {
        for (JTextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
