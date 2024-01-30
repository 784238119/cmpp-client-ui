package com.calo.cmpp.domain;

import com.calo.cmpp.enums.MobileType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMessageGenerating {

    private Integer sendAccountId;
    private String mobile;
    private MobileType mobileType;
    private String content;
    private boolean isRandomContent;
    private String extensionCode;
    private boolean continuousGeneration;
    private Integer sendSize;
    private Integer generationSpeedNum;
}
