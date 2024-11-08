package naumenproject.naumenproject;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.model.UserPassword;
import naumenproject.naumenproject.repository.UserPasswordRepository;
import naumenproject.naumenproject.service.EncodeService;
import naumenproject.naumenproject.service.PasswordService;
import naumenproject.naumenproject.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        Mockito.when(encodeService.encryptData(password)).thenReturn(encodedPassword);
        Mockito.when(userService.getUserByTelegramId(userTelegramId)).thenReturn(new User());

        passwordService.createUserPassword(password, description, userTelegramId);

        Mockito.verify(userPasswordRepository, Mockito.times(1)).save(ArgumentMatchers.any(UserPassword.class));
        Mockito.verify(encodeService, Mockito.times(1)).encryptData(password);
    }

    /**
     * Тест получения пароля
     */
    @Test
    void testGetUserPasswords() {
        long userTelegramId = 12345L;
        List<UserPassword> passwords = List.of(new UserPassword(), new UserPassword());

        Mockito.when(userPasswordRepository.findByUserTelegramId(userTelegramId)).thenReturn(passwords);

        List<UserPassword> result = passwordService.getUserPasswords(userTelegramId);

        Assertions.assertEquals(passwords, result);
        Mockito.verify(userPasswordRepository, Mockito.times(1)).findByUserTelegramId(userTelegramId);
    }

    /**
     * Тест поиска пароля по UUID
     */
    @Test
    void testFindPasswordByUuid() {
        String passUuid = UUID.randomUUID().toString();
        String userUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword password = new UserPassword(passUuid, "desc", "pass", user);

        Mockito.when(userPasswordRepository.findByUuid(passUuid)).thenReturn(password);

        UserPassword expectedPass = passwordService.findPasswordByUuid(passUuid);

        Mockito.verify(userPasswordRepository, Mockito.times(1)).findByUuid(passUuid);
        Assertions.assertEquals(expectedPass, password);
    }

    /**
     * Тест удаления пароля
     */
    @Test
    void testDeletePassword() {
        String uuid = UUID.randomUUID().toString();

        Mockito.when(userPasswordRepository.existsByUuid(uuid)).thenReturn(true);

        passwordService.deletePassword(uuid);

        Mockito.verify(userPasswordRepository, Mockito.times(1)).deleteByUuid(uuid);
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

        Mockito.when(userPasswordRepository.existsByUuid(passUuid)).thenReturn(true);
        Mockito.when(userPasswordRepository.findByUuid(passUuid)).thenReturn(pass);
        Mockito.when(encodeService.encryptData(newPass)).thenReturn(encodedPass);

        passwordService.updatePassword(passUuid, newDesc, newPass);

        Mockito.verify(userPasswordRepository, Mockito.times(1)).save(pass);
        Mockito.verify(encodeService, Mockito.times(1)).encryptData(newPass);
        Assertions.assertEquals(newDesc, pass.getDescription());
        Assertions.assertEquals(encodedPass, pass.getPassword());
    }

    /**
     * Тест генерации пароля для сложности 1
     */
    @Test
    void testGeneratePassword_WithComplexity_Level1() {
        int length = 10;
        int complexity = 1;

        String password = passwordService.generatePasswordWithComplexity(length, complexity);

        Assertions.assertEquals(length, password.length());
        Assertions.assertTrue(password.matches("^[a-z]+$"));
    }

    /**
     * Тест генерации пароля для сложности 2
     */
    @Test
    void testGeneratePassword_WithComplexity_Level2() {
        int length = 12;
        int complexity = 2;

        String password = passwordService.generatePasswordWithComplexity(length, complexity);

        Assertions.assertEquals(length, password.length());
        Assertions.assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$"));
    }

    /**
     * Тест генерации пароля для сложности 3
     */
    @Test
    void testGeneratePassword_WithComplexity_Level3() {
        int length = 15;
        int complexity = 3;

        String password = passwordService.generatePasswordWithComplexity(length, complexity);

        Assertions.assertEquals(length, password.length());
        Assertions.assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?])[a-zA-Z\\d!@#$%^&*()\\-_=+<>?]+$"));
    }

    /**
     * Тест уникальности паролей
     */
    @RepeatedTest(5)
    void testGenerateUniquePassword() {
        int length = 10;
        int complexity = 2;

        String password1 = passwordService.generatePasswordWithComplexity(length, complexity);
        String password2 = passwordService.generatePasswordWithComplexity(length, complexity);

        Assertions.assertNotEquals(password1, password2);
    }
}
