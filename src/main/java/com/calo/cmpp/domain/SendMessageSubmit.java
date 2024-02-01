package com.calo.cmpp.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SendMessageSubmit {

    private Integer channelId;

    private String localMessageId;

    private String mobile;

    private String extend;

    private String content;

    private int count;

    private List<String> msgId = new ArrayList<>();

    private List<String> status = new ArrayList<>();

    @Override
    public String toString() {
        return "[" +
               "channelId='" + channelId + '\'' +
               ", mobile='" + mobile + '\'' +
               ", extend='" + extend + '\'' +
               ", content='" + content + '\'' +
               ", count=" + count +
               ", msgId=" + msgId +
               ", status=" + status +
               ']';
    }
}
