package com.ctxh.volunteer.module.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class NimbusDecoderConfig {

    @Bean
    public NimbusJwtDecoder nimbusJwtDecoder(RSAKeyRecord rsaKeyRecord) {
        return NimbusJwtDecoder
                .withPublicKey(rsaKeyRecord.rsaPublicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }
}
