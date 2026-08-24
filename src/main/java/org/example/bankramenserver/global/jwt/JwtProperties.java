package org.example.bankramenserver.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secretKey;
    private String header;
    private String prefix;
    private long accessExp;
    private long refreshExp;
}
