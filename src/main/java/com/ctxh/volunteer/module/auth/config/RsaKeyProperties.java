package com.ctxh.volunteer.module.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class RsaKeyProperties {
    /**
     * Path to the private key file (e.g., classpath:certs/privateKey.pem)
     */
    private String rsaPrivateKey;

    /**
     * Path to the public key file (e.g., classpath:certs/publicKey.pem)
     */
    private String rsaPublicKey;

    /**
     * Raw content of the private key (PEM format). Overrides rsaPrivateKey path if present.
     */
    private String rsaPrivateKeyContent;

    /**
     * Raw content of the public key (PEM format). Overrides rsaPublicKey path if present.
     */
    private String rsaPublicKeyContent;

    private String keyId;
}
