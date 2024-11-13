package ru.naumen;

import ru.naumen.service.EncodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        encodeService = new EncodeService("BC1D9VG58QKH6CYK99DGB1UBESR8VRXD");
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
