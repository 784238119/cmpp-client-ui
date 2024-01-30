package com.calo.cmpp.event;

import com.calo.cmpp.domain.CmppChannelAccount;
import com.calo.cmpp.enums.OperateType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountOperateEvent extends ApplicationEvent {

    private final OperateType operateType;
    private final CmppChannelAccount cmppChannelAccount;

    public AccountOperateEvent(Object source, OperateType operateType, CmppChannelAccount cmppChannelAccount) {
        super(source);
        this.operateType = operateType;
        this.cmppChannelAccount = cmppChannelAccount;
    }
}
