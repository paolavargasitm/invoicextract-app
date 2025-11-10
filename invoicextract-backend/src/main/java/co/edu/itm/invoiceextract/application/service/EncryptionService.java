package co.edu.itm.invoiceextract.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int KEY_LENGTH = 32; // 256 bits for AES-256

    @Value("${encryption.secret-key}")
    private String secretKey;

    /**
     * Encrypts a value using both the main secret key and a per-configuration encryption key
     * @param valueToEnc The value to encrypt
     * @param configEncryptionKey The per-configuration encryption key (should be stored with the config)
     * @return Encrypted string in Base64 format
     */
    public String encrypt(String valueToEnc, String configEncryptionKey) throws Exception {
        // Use the configEncryptionKey raw UTF-8 bytes as AES key (must be 16/24/32 bytes)
        byte[] rawKey = configEncryptionKey.getBytes(StandardCharsets.UTF_8);
        if (rawKey.length != 16 && rawKey.length != 24 && rawKey.length != 32) {
            throw new IllegalArgumentException("encryptionKey must be 16, 24, or 32 bytes; got: " + rawKey.length);
        }
        SecretKeySpec key = new SecretKeySpec(rawKey, ALGORITHM);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] encryptedByteValue = cipher.doFinal(valueToEnc.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedByteValue);
    }

    /**
     * Decrypts a value using both the main secret key and the per-configuration encryption key
     * @param encryptedValue The encrypted value in Base64 format
     * @param configEncryptionKey The per-configuration encryption key (should be stored with the config)
     * @return Decrypted string
     */
    public String decrypt(String encryptedValue, String configEncryptionKey) throws Exception {
        // Use the configEncryptionKey raw UTF-8 bytes as AES key (must be 16/24/32 bytes)
        byte[] rawKey = configEncryptionKey.getBytes(StandardCharsets.UTF_8);
        if (rawKey.length != 16 && rawKey.length != 24 && rawKey.length != 32) {
            throw new IllegalArgumentException("encryptionKey must be 16, 24, or 32 bytes; got: " + rawKey.length);
        }
        SecretKeySpec key = new SecretKeySpec(rawKey, ALGORITHM);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        
        byte[] decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedByteValue, StandardCharsets.UTF_8);
    }
    
    /**
     * Generates a new random encryption key
     * @return A new random UUID-based key
     */
    public String generateEncryptionKey() {
        // 32 ASCII chars so UTF-8 bytes are exactly 32 (AES-256 compatible)
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
