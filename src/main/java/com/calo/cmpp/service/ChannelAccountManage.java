package com.calo.cmpp.service;

import cn.hutool.extra.spring.SpringUtil;
import com.calo.cmpp.domain.CmppChannelAccount;
import com.calo.cmpp.handler.CmppSessionHandler;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelAccountManage {

    private static final Map<Integer,CmppChannelAccount> account = new ConcurrentHashMap<>();

    public synchronized static void addAccount(CmppChannelAccount cmppChannelAccount) {
        if (cmppChannelAccount.getId() != null && account.get(cmppChannelAccount.getId()) != null) {
            removeAccount(cmppChannelAccount.getId());
            account.put(cmppChannelAccount.getId(), cmppChannelAccount);
        } else {
            cmppChannelAccount.setId(account.size());
            account.put(cmppChannelAccount.getId(), cmppChannelAccount);
        }
        EndpointEntity cmppClient = getCmppClient(cmppChannelAccount);
        EndpointManager.INS.openEndpoint(cmppClient);
    }

    public static EndpointEntity getCmppClient(CmppChannelAccount cmppAccount) {
        CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
        client.setId(String.valueOf(cmppAccount.getId()));
        client.setHost(cmppAccount.getHost());
        client.setPort(Integer.valueOf(cmppAccount.getPort()));
        client.setChartset(StandardCharsets.UTF_8);
        client.setGroupName(cmppAccount.getChannelName());
        client.setUserName(cmppAccount.getUsername());
        client.setPassword(cmppAccount.getPassword());
        client.setSpCode(cmppAccount.getSrcId());
        client.setServiceId(cmppAccount.getSrcId());
        client.setMsgSrc(cmppAccount.getUsername());
        client.setMaxChannels(Short.parseShort(cmppAccount.getMaxConnect().toString()));
        client.setCloseWhenRetryFailed(false);
        client.setVersion(cmppAccount.getVersion().getValue());
        client.setRetryWaitTimeSec((short) 60);
        client.setUseSSL(false);
        client.setWriteLimit(cmppAccount.getSpeed() * 2);
        client.setReadLimit(cmppAccount.getSpeed() * 2);
        client.setReSendFailMsg(false);
        client.setSupportLongmsg(EndpointEntity.SupportLongMessage.BOTH);
        client.setBusinessHandlerSet(List.of(SpringUtil.getBean(CmppSessionHandler.class)));
        return client;
    }

    public synchronized static void removeAccount(int id) {
        EndpointConnector<?> connector = EndpointManager.INS.getEndpointConnector(String.valueOf(id));
        if (connector != null) {
            try {
                connector.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        EndpointManager.INS.remove(String.valueOf(id));
        account.remove(id);
    }

    public synchronized static CmppChannelAccount getAccount(int id) {
        return account.get(id);
    }

    public synchronized static List<CmppChannelAccount> getAccountAll() {
        return account.values().stream().toList();
    }

    public static int getSize() {
        return account.size();
    }
}
