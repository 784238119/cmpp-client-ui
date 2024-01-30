package com.calo.cmpp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.calo.cmpp.config.BusinessThreadPool;
import com.calo.cmpp.domain.CmppAccountTableModel;
import com.calo.cmpp.domain.CmppChannelAccount;
import com.calo.cmpp.enums.OperateType;
import com.calo.cmpp.event.AccountOperateEvent;
import com.calo.cmpp.event.RightClickMouseListener;
import com.calo.cmpp.module.AccountFormDialog;
import com.calo.cmpp.module.CustomTableCellRenderer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Log4j2
@Component
public class AccountForm extends JScrollPane implements MonitorWin {

    @Resource
    private LogMonitor logMonitor;
    private static final String ACCOUNT_DB_PATH = "account.db";
    @Getter
    private final CmppAccountTableModel cmppAccountTableModel = new CmppAccountTableModel();
    private final JTable table = new JTable(cmppAccountTableModel);

    {
        table.setComponentPopupMenu(createPopupMenu());
        this.setViewportView(table);
        this.setBorder(BorderFactory.createEmptyBorder()); // 清除默认边框
        this.setViewportBorder(BorderFactory.createEmptyBorder()); // 清除视口边框
        this.addMouseListener(new RightClickMouseListener(createPopupMenu()));
        initTable();
    }

    @PostConstruct
    private void initChannelAccount() {
        File file = new File(ACCOUNT_DB_PATH);
        if (!file.exists()) {
            return;
        }

        List<String> stringList = FileUtil.readLines(file, Charset.defaultCharset());
        for (String string : stringList) {
            CmppChannelAccount bean = JSONUtil.parseObj(string).toBean(CmppChannelAccount.class);
            cmppAccountTableModel.addAccount(bean);
            logMonitor.appendLog("加载账号：" + bean.getChannelName());
        }

    }

    @PreDestroy
    public void saveAccount() {
        List<String> stringList = cmppAccountTableModel.getAccountAll().stream().filter(Objects::nonNull).map(JSONUtil::toJsonStr).toList();
        File file = new File(ACCOUNT_DB_PATH);
        if (file.exists()) {
            FileUtil.del(ACCOUNT_DB_PATH);
        }
        log.info("channel account db path:{}", file.getAbsolutePath());
        FileUtil.appendLines(stringList, file, Charset.defaultCharset());
    }

    @PostConstruct
    private void initTable() {
        // 设置内容高度
        table.setRowHeight(30);
        // 设置表格内容不可编辑
        table.setEnabled(true);
        // 设置表头不可拖动
        table.getTableHeader().setReorderingAllowed(false);
        // 设置表头不可点击
        table.getTableHeader().setResizingAllowed(false);
        // 设置表头高度
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        // 设置字体大小
        table.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        // 设置允许选中内容
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 表头居中
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        // 设置每一列的渲染器
        for (int i = 0; i < cmppAccountTableModel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new CustomTableCellRenderer());
        }
        BusinessThreadPool.getBusiGroup().scheduleAtFixedRate(cmppAccountTableModel::fireTableDataChanged, 0, 10000, TimeUnit.MILLISECONDS);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem1 = new JMenuItem("新增账号");
        menuItem1.addActionListener(e -> new AccountFormDialog(new CmppChannelAccount(), cmppAccountTableModel));
        popupMenu.add(menuItem1);

        JMenuItem menuItem2 = new JMenuItem("修改账号");
        menuItem2.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                CmppChannelAccount selectedAccount = cmppAccountTableModel.getAccount(selectedRow);
                if (selectedAccount != null) {
                    new AccountFormDialog(selectedAccount, cmppAccountTableModel);
                    logMonitor.appendLog("修改账号：" + selectedAccount.getChannelName());
                }
            }
        });
        popupMenu.add(menuItem2);

        JMenuItem menuItem3 = new JMenuItem("删除账号");
        menuItem3.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int option = JOptionPane.showConfirmDialog(null, "确定删除选中的账号吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    CmppChannelAccount account = cmppAccountTableModel.getAccount(selectedRow);
                    cmppAccountTableModel.removeAccount(selectedRow);
                    if (account == null) {
                        return;
                    }
                    logMonitor.appendLog("删除账号：" + account.getChannelName());
                }
            }
        });
        popupMenu.add(menuItem3);

        JMenuItem menuItem4 = new JMenuItem("刷新账号");
        menuItem4.addActionListener(e -> {
            cmppAccountTableModel.fireTableDataChanged();
        });
        popupMenu.add(menuItem4);
        return popupMenu;
    }

    @Override
    public void refreshPageRendering() {
        table.updateUI();
    }
}

