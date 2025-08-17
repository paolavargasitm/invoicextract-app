package co.edu.itm.invoiceextract.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        byte[] combinedKey = generateCombinedKey(configEncryptionKey);
        SecretKeySpec key = new SecretKeySpec(combinedKey, ALGORITHM);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
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
        byte[] combinedKey = generateCombinedKey(configEncryptionKey);
        SecretKeySpec key = new SecretKeySpec(combinedKey, ALGORITHM);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        
        byte[] decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedByteValue, StandardCharsets.UTF_8);
    }
    
    /**
     * Generates a combined key using both the main secret key and the configuration-specific key
     * @param configKey The per-configuration encryption key
     * @return Combined key bytes
     */
    private byte[] generateCombinedKey(String configKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        String combined = secretKey + "|" + configKey;
        byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
        
        // Ensure the key is the correct length for the algorithm
        return Arrays.copyOf(hash, KEY_LENGTH);
    }
    
    /**
     * Generates a new random encryption key
     * @return A new random UUID-based key
     */
    public String generateEncryptionKey() {
        // Using UUID to generate a random string and then hashing it
        String randomUUID = UUID.randomUUID().toString();
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(randomUUID.getBytes(StandardCharsets.UTF_8));
            // Convert to hex string and take first 64 chars (256 bits)
            return bytesToHex(hash).substring(0, 64);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simpler random string if hashing fails (shouldn't happen with SHA-256)
            return randomUUID.replace("-", "");
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
