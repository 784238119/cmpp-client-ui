package com.calo.cmpp.enums;

import lombok.Getter;

@Getter
public enum MobileType {

    RAND("随机", 0), CHINA_MOBILE("移动", 1), CHINA_UNICOM("联通", 2), CHINA_TELECOME("电信", 3);

    MobileType(String name, int value) {
        this.name = name;
        this.value = value;
    }


    private final String name;
    private final int value;

    @Override
    public String toString() {
        return name;
    }


}
