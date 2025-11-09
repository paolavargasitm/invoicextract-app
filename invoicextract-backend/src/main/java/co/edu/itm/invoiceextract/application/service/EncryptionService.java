package co.edu.itm.invoiceextract.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;
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
    // GCM/PBKDF2 parameters
    private static final int SALT_LEN = 16; // 128-bit salt
    private static final int IV_LEN = 12;   // 96-bit nonce recommended for GCM
    private static final int TAG_BITS = 128; // 16 bytes tag
    private static final int ITERATIONS = 100_000; // PBKDF2 iterations
    private static final int KEY_BITS = 256; // AES-256

    @Value("${encryption.secret-key}")
    private String secretKey;

    /**
     * Encrypts a value using both the main secret key and a per-configuration encryption key
     * @param valueToEnc The value to encrypt
     * @return Encrypted string in Base64 format
     */
    public String encrypt(String valueToEnc) throws Exception {
        // New scheme: AES-256-GCM with PBKDF2 key derivation and random salt/iv
        SecureRandom rnd = new SecureRandom();
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        rnd.nextBytes(salt);
        rnd.nextBytes(iv);

        SecretKeySpec key = deriveKey(secretKey, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] ctWithTag = cipher.doFinal(valueToEnc.getBytes(StandardCharsets.UTF_8));

        int tagLen = TAG_BITS / 8;
        byte[] ct = Arrays.copyOfRange(ctWithTag, 0, ctWithTag.length - tagLen);
        byte[] tag = Arrays.copyOfRange(ctWithTag, ctWithTag.length - tagLen, ctWithTag.length);

        return Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(iv) + ":" +
               Base64.getEncoder().encodeToString(ct) + ":" +
               Base64.getEncoder().encodeToString(tag);
    }

    /**
     * Decrypts a value using both the main secret key and the per-configuration encryption key
     * @param encryptedValue The encrypted value in Base64 format
     * @param configEncryptionKey The per-configuration encryption key (should be stored with the config)
     * @return Decrypted string
     */
    public String decrypt(String encryptedValue, String configEncryptionKey) throws Exception {
        // Try new GCM format first: salt:iv:ciphertext:tag
        String[] parts = encryptedValue.split(":");
        if (parts.length == 4) {
            try {
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                byte[] iv = Base64.getDecoder().decode(parts[1]);
                byte[] ct = Base64.getDecoder().decode(parts[2]);
                byte[] tag = Base64.getDecoder().decode(parts[3]);

                SecretKeySpec key = deriveKey(secretKey, salt);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
                byte[] ctWithTag = new byte[ct.length + tag.length];
                System.arraycopy(ct, 0, ctWithTag, 0, ct.length);
                System.arraycopy(tag, 0, ctWithTag, ct.length, tag.length);
                byte[] pt = cipher.doFinal(ctWithTag);
                return new String(pt, StandardCharsets.UTF_8);
            } catch (Exception ignore) {
                // fall through to legacy
            }
        }

        // Legacy fallback: AES/ECB/PKCS5Padding using prior combined key (SHA-256 of secretKey)
        byte[] combinedKey = generateCombinedKey();
        SecretKeySpec key = new SecretKeySpec(combinedKey, ALGORITHM);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedByteValue, StandardCharsets.UTF_8);
    }
    
    /**
     * Generates a combined key using both the main secret key and the configuration-specific key
     * @return Combined key bytes
     */
    private byte[] generateCombinedKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        String combined = secretKey;
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
    
    // PBKDF2 key derivation using master secret and per-message salt
    private static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(skf.generateSecret(spec).getEncoded(), ALGORITHM);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
