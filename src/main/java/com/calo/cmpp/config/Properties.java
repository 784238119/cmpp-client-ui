package com.calo.cmpp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("software")
public class Properties {
    private String name;
    private String version;
}
