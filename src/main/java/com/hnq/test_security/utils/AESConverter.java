package com.hnq.test_security.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter(autoApply = false) // không tự động áp dụng với mọi String
public class AESConverter implements AttributeConverter<String, String> {

    // Key và IV nên được load từ cấu hình, đây chỉ là demo
    private static final String SECRET_KEY_BASE64 =
            "xM1q4aVx+FqXyZoC0m5DQBt0UOjozN3kln5bA67v9Qk="; // Ví dụ key mã hóa
    // sẵn
    private static final String INIT_VECTOR = "1234567890abcdef"; // 16 bytes

    private final SecretKey secretKey;

    public AESConverter() {
        this.secretKey = AESEncryptionUtils.decodeKeyFromBase64(SECRET_KEY_BASE64);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            return AESEncryptionUtils.encrypt(attribute, secretKey, INIT_VECTOR.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa AES", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return AESEncryptionUtils.decrypt(dbData, secretKey, INIT_VECTOR.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã AES", e);
        }
    }
}
