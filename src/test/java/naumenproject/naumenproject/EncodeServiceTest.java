package naumenproject.naumenproject;

import naumenproject.naumenproject.service.EncodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Класс модульных тестов для CommandService
 */
public class EncodeServiceTest {

    private EncodeService encodeService;

    /**
     * Задаёт поле с ключом шифрования
     */
    @BeforeEach
    void setUp() {
        encodeService = new EncodeService();
        String TEST_KEY = "BC1D9VG58QKH6CYK99DGB1UBESR8VRXD";
        ReflectionTestUtils.setField(encodeService, "secretKey", TEST_KEY);
    }

    /**
     * Тест шифрования
     */
    @Test
    public void encryptDataTest() {
        String plainText = "text123";
        String expectedText = "bQd+8RRXsEd8DaOsQFkGmw==";
        String encryptedText = encodeService.encryptData(plainText);

        Assertions.assertNotNull(encryptedText);
        Assertions.assertEquals(expectedText, encryptedText);
    }

    /**
     * Тест дешифрования
     */
    @Test
    public void decryptDataTest() {
        String expectedText = "text123";
        String encryptedText = "bQd+8RRXsEd8DaOsQFkGmw==";
        String decryptedText = encodeService.decryptData(encryptedText);

        Assertions.assertNotNull(decryptedText);
        Assertions.assertEquals(expectedText, decryptedText);
    }
}
