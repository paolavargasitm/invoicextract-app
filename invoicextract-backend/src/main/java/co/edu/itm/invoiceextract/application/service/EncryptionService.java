package co.edu.itm.invoiceextract.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";

    @Value("${encryption.secret-key}")
    private String secretKey;

    public String encrypt(String valueToEnc) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByteValue = cipher.doFinal(valueToEnc.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedByteValue);
    }

    public String decrypt(String encryptedValue) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedByteValue, StandardCharsets.UTF_8);
    }
}
