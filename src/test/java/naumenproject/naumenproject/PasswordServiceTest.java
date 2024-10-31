package naumenproject.naumenproject;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.model.UserPassword;
import naumenproject.naumenproject.repository.UserPasswordRepository;
import naumenproject.naumenproject.service.EncodeService;
import naumenproject.naumenproject.service.PasswordService;
import naumenproject.naumenproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Класс модульных тестов для PasswordService
 */
class PasswordServiceTest {

    @Mock
    private EncodeService encodeService;

    @Mock
    private UserService userService;

    @Mock
    private UserPasswordRepository userPasswordRepository;

    @InjectMocks
    private PasswordService passwordService;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест создания объекта пароля
     */
    @Test
    void testCreateUserPassword() {
        String password = "pass";
        String encodedPassword = "encPass";
        String description = "desc";
        long userTelegramId = 12345L;

        when(encodeService.encryptData(password)).thenReturn(encodedPassword);
        when(userService.getUserByTelegramId(userTelegramId)).thenReturn(new User());

        passwordService.createUserPassword(password, description, userTelegramId);

        verify(userPasswordRepository, times(1)).save(any(UserPassword.class));
        verify(encodeService, times(1)).encryptData(password);
    }

    /**
     * Тест получения пароля
     */
    @Test
    void testGetUserPasswords() {
        long userTelegramId = 12345L;
        List<UserPassword> passwords = List.of(new UserPassword(), new UserPassword());

        when(userPasswordRepository.findByUserTelegramId(userTelegramId)).thenReturn(passwords);

        List<UserPassword> result = passwordService.getUserPasswords(userTelegramId);

        assertEquals(passwords, result);
        verify(userPasswordRepository, times(1)).findByUserTelegramId(userTelegramId);
    }

    /**
     * Тест удаления пароля
     */
    @Test
    void testDeletePassword() {
        String uuid = UUID.randomUUID().toString();

        when(userPasswordRepository.existsByUuid(uuid)).thenReturn(true);

        passwordService.deletePassword(uuid);

        verify(userPasswordRepository, times(1)).deleteByUuid(uuid);
    }

    /**
     * Тест обновления пароля
     */
    @Test
    void testUpdatePassword() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        String newPass = "newPass";
        String newDesc = "newDesc";
        String encodedPass = "encPass";

        when(userPasswordRepository.existsByUuid(passUuid)).thenReturn(true);
        when(userPasswordRepository.findByUuid(passUuid)).thenReturn(pass);
        when(encodeService.encryptData(newPass)).thenReturn(encodedPass);

        passwordService.updatePassword(passUuid, newDesc, newPass);

        verify(userPasswordRepository, times(1)).save(pass);
        verify(encodeService, times(1)).encryptData(newPass);
        assertEquals(newDesc, pass.getDescription());
        assertEquals(encodedPass, pass.getPassword());
    }

    /**
     * Тест генерации пароля для сложности 1
     */
    @Test
    void testGeneratePassword_Level1() {
        int length = 10;
        int complexity = 1;

        String password = passwordService.generatePassword(length, complexity);

        assertEquals(length, password.length());
        assertTrue(password.matches("^[a-z]+$"));
    }

    /**
     * Тест генерации пароля для сложности 2
     */
    @Test
    void testGeneratePassword_Level2() {
        int length = 12;
        int complexity = 2;

        String password = passwordService.generatePassword(length, complexity);

        assertEquals(length, password.length());
        assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$"));
    }

    /**
     * Тест генерации пароля для сложности 3
     */
    @Test
    void testGeneratePassword_Level3() {
        int length = 15;
        int complexity = 3;

        String password = passwordService.generatePassword(length, complexity);

        assertEquals(length, password.length());
        assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?])[a-zA-Z\\d!@#$%^&*()\\-_=+<>?]+$"));
    }

    /**
     * Тест уникальности паролей
     */
    @RepeatedTest(5)
    void testGenerateUniquePassword() {
        int length = 10;
        int complexity = 2;

        String password1 = passwordService.generatePassword(length, complexity);
        String password2 = passwordService.generatePassword(length, complexity);

        assertNotEquals(password1, password2);
    }
}
