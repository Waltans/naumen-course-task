package naumenproject.naumenproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Класс для работы с шифрованием паролей
 */
@Service
public class EncodeService {

    private final String ALGORITHM = "AES";

    @Value("${auth.password-encrypt-key}")
    private String secretKey;

    /**
     * Шифрует строку алгоритмом AES по заданному ключу
     *
     * @param plainString строка
     * @return шифрованная строка
     */
    public String encryptData(String plainString) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainString.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Расшифровывает строку алгоритмом AES по заданному ключу
     *
     * @param encryptedString шифрованная строка
     * @return расшифрованная строка
     */
    public String decryptData(String encryptedString) {
        try {
            encryptedString = encryptedString.replaceAll("\\s", "+");
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
