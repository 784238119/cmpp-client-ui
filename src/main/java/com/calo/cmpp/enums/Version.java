package com.calo.cmpp.enums;

import lombok.Getter;

@Getter
public enum Version {

    CMPP20((short) 0x20), CMPP30((short) 0x30);

    Version(short value) {
        this.value = value;
    }

    private final short value;

}
