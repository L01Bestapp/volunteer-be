package com.ctxh.volunteer.module.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RsaKeyConfig {

    private final RsaKeyProperties rsaKeyProperties;
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Bean
    public RSAKeyRecord rsaKeyRecord() {
        RSAPublicKey publicKey = loadPublicKey();
        RSAPrivateKey privateKey = loadPrivateKey();
        return new RSAKeyRecord(publicKey, privateKey, rsaKeyProperties.getKeyId());
    }

    private RSAPublicKey loadPublicKey() {
        try {
            String keyContent;
            if (rsaKeyProperties.getRsaPublicKeyContent() != null && !rsaKeyProperties.getRsaPublicKeyContent().isBlank()) {
                log.info("Loading RSA Public Key from environment content");
                keyContent = rsaKeyProperties.getRsaPublicKeyContent();
            } else {
                log.info("Loading RSA Public Key from file: {}", rsaKeyProperties.getRsaPublicKey());
                keyContent = loadFileContent(rsaKeyProperties.getRsaPublicKey());
            }
            return parsePublicKey(keyContent);
        } catch (Exception e) {
            log.error("Failed to load RSA Public Key: {}", e.getMessage());
            throw new RuntimeException("Could not load RSA Public Key", e);
        }
    }

    private RSAPrivateKey loadPrivateKey() {
        try {
            String keyContent;
            if (rsaKeyProperties.getRsaPrivateKeyContent() != null && !rsaKeyProperties.getRsaPrivateKeyContent().isBlank()) {
                log.info("Loading RSA Private Key from environment content");
                keyContent = rsaKeyProperties.getRsaPrivateKeyContent();
            } else {
                log.info("Loading RSA Private Key from file: {}", rsaKeyProperties.getRsaPrivateKey());
                keyContent = loadFileContent(rsaKeyProperties.getRsaPrivateKey());
            }
            return parsePrivateKey(keyContent);
        } catch (Exception e) {
            log.error("Failed to load RSA Private Key: {}", e.getMessage());
            throw new RuntimeException("Could not load RSA Private Key", e);
        }
    }

    private String loadFileContent(String path) {
        if (path == null) {
            throw new IllegalArgumentException("RSA Key path is null");
        }
        Resource resource = resourceLoader.getResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read resource: " + path, e);
        }
    }

    private RSAPublicKey parsePublicKey(String keyContent) throws Exception {
        String publicKeyPEM = keyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", ""); // Remove newlines and spaces

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private RSAPrivateKey parsePrivateKey(String keyContent) throws Exception {
        String privateKeyPEM = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "") // Handle PKCS#1 if present (though PKCS#8 is preferred)
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // Remove newlines and spaces

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
