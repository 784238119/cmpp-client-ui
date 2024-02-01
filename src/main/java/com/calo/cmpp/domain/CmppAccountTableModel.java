package com.calo.cmpp.domain;

import com.calo.cmpp.enums.Version;
import com.calo.cmpp.service.ChannelAccountManage;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
public class CmppAccountTableModel extends AbstractTableModel {

    private final String[] tableNames = {"名称", "地址", "端口", "协议", "账号", "密码", "码号", "速度", "连接", "最大连接"};

    public CmppChannelAccount getAccount(Integer id) {
        return ChannelAccountManage.getAccount(id);
    }

    public List<CmppChannelAccount> getAccountAll() {
        return ChannelAccountManage.getAccountAll();
    }

    public void addAccount(CmppChannelAccount cmppChannelAccount) {
        ChannelAccountManage.addAccount(cmppChannelAccount);
        fireTableDataChanged();
    }

    public void removeAccount(int id) {
        ChannelAccountManage.removeAccount(id);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return ChannelAccountManage.getSize();
    }

    @Override
    public int getColumnCount() {
        return tableNames.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column >= 0 && column < tableNames.length) {
            return tableNames[column];
        }
        return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CmppChannelAccount cmppChannelAccount = ChannelAccountManage.getAccount(rowIndex);
        if (cmppChannelAccount == null) {
            return null;
        }
        EndpointConnector<?> connector = EndpointManager.INS.getEndpointConnector(String.valueOf(cmppChannelAccount.getId()));
        return switch (columnIndex) {
            case 0 -> cmppChannelAccount.getChannelName();
            case 1 -> cmppChannelAccount.getHost();
            case 2 -> cmppChannelAccount.getPort();
            case 3 -> cmppChannelAccount.getVersion();
            case 4 -> cmppChannelAccount.getUsername();
            case 5 -> cmppChannelAccount.getPassword();
            case 6 -> cmppChannelAccount.getSrcId();
            case 7 -> cmppChannelAccount.getSpeed();
            case 8 -> connector == null ? 0 : connector.getConnectionNum();
            case 9 -> cmppChannelAccount.getMaxConnect();
            default -> null;
        };
    }
}
