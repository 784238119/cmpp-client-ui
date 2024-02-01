package com.calo.cmpp.domain;

import com.calo.cmpp.enums.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmppChannelAccount implements Serializable {

    private Integer id;
    private String channelName;
    private String host;
    private String port = "7890";
    private Version version = Version.CMPP20;
    private String username;
    private String password;
    private String srcId;
    private Integer speed = 200;
    private Integer maxConnect = 2;

    @Override
    public String toString() {
        return channelName;
    }
}
