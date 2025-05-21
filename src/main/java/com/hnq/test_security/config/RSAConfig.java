package com.hnq.test_security.config;

import com.hnq.test_security.utils.RSAEncryptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class RSAConfig {

    /**
     * Luôn tự sinh mới KeyPair mỗi lần khởi động.
     */
    @Bean
    public KeyPair keyPair() throws Exception {
        KeyPair keyPair = RSAEncryptionUtils.generateRSAKeyPair();

        System.out.println("Generated new RSA KeyPair:");
        System.out.println("PUBLIC_KEY_BASE64=" + RSAEncryptionUtils.encodePublicKeyToBase64(keyPair.getPublic()));
        System.out.println("PRIVATE_KEY_BASE64=" + RSAEncryptionUtils.encodePrivateKeyToBase64(keyPair.getPrivate()));

        return keyPair;
    }

    @Bean
    public PrivateKey privateKey(KeyPair keyPair) {
        return keyPair.getPrivate();
    }

    @Bean
    public PublicKey publicKey(KeyPair keyPair) {
        return keyPair.getPublic();
    }
}
