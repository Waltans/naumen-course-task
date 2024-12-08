package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.naumen.exception.*;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.repository.UserPasswordRepository;

import java.time.LocalDate;
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
    void testCreateUserPassword() throws UserNotFoundException {
        String password = "pass";
        String encodedPassword = "encPass";
        String description = "desc";
        long userId = 12345L;

        Mockito.when(encodeService.encryptData(password)).thenReturn(encodedPassword);
        Mockito.when(userService.getUserById(userId)).thenReturn(new User());

        passwordService.createUserPassword(password, description, userId);

        Mockito.verify(userPasswordRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(UserPassword.class));
    }

    /**
     * Тест получения пароля
     */
    @Test
    void testGetUserPasswords() {
        long userId = 12345L;
        List<UserPassword> passwords = List.of(new UserPassword(), new UserPassword());

        Mockito.when(userPasswordRepository.findByUserId(userId)).thenReturn(passwords);

        List<UserPassword> result = passwordService.getUserPasswords(userId);

        Assertions.assertEquals(passwords, result);
    }

    /**
     * Тест поиска паролей по частичному описанию
     */
    @Test
    void testGetUserPasswordsWithPartialDescription() {
        long userId = 12345L;
        String searchRequest = "mat";
        List<UserPassword> passwords = List.of(
                new UserPassword(UUID.randomUUID().toString(), "match", "pass1", new User(), LocalDate.now()),
                new UserPassword(UUID.randomUUID().toString(), "mat123", "pass2", new User(), LocalDate.now())
        );

        Mockito.when(userPasswordRepository.findByDescriptionContainsIgnoreCaseAndUserId(searchRequest, userId))
                .thenReturn(passwords);

        List<UserPassword> result = passwordService.getUserPasswordsWithPartialDescription(userId, searchRequest);

        Assertions.assertEquals(passwords, result);
    }

    /**
     * Тест получения отсортированных паролей по дате
     */
    @Test
    void testGetUserPasswordsSorted_ByDate() throws IncorrectSortTypeException {
        long userId = 12345L;
        List<UserPassword> passwords = List.of(
                new UserPassword("uuid", "desc1", "pass1", new User(), LocalDate.of(2021, 1, 1)),
                new UserPassword("uuid", "desc2", "pass2", new User(), LocalDate.of(2023, 1, 1))
        );

        Mockito.when(userPasswordRepository.findByUserIdOrderByLastModifyDate(userId)).thenReturn(passwords);

        List<UserPassword> result = passwordService.getUserPasswordsSorted(userId, SortType.BY_DATE);

        Assertions.assertEquals(passwords, result);
    }

    /**
     * Тест получения отсортированных паролей по описанию
     */
    @Test
    void testGetUserPasswordsSorted_ByDescription() throws IncorrectSortTypeException {
        long userId = 12345L;
        List<UserPassword> passwords = List.of(
                new UserPassword(UUID.randomUUID().toString(), "A desc", "pass1", new User(), LocalDate.now()),
                new UserPassword(UUID.randomUUID().toString(), "Z desc", "pass2", new User(), LocalDate.now())
        );

        Mockito.when(userPasswordRepository.findByUserIdOrderByDescriptionAsc(userId)).thenReturn(passwords);

        List<UserPassword> result = passwordService.getUserPasswordsSorted(userId, SortType.BY_DESCRIPTION);

        Assertions.assertEquals(passwords, result);
    }

    /**
     * Тест валидации индекса пароля при валидном индексе
     */
    @Test
    void testIsValidPasswordIndex() {
        long userId = 12345L;
        List<UserPassword> passwords = List.of(new UserPassword(), new UserPassword());

        Mockito.when(userPasswordRepository.countByUserId(userId)).thenReturn(passwords.size());

        Assertions.assertTrue(passwordService.isValidPasswordIndex(2, 12345L));
    }

    /**
     * Тест валидации индекса пароля при невалидном индексе
     */
    @Test
    void testIsInvalidPasswordIndex() {
        long userId = 12345L;
        List<UserPassword> passwords = List.of(new UserPassword(), new UserPassword());

        Mockito.when(userPasswordRepository.countByUserId(userId)).thenReturn(passwords.size());

        Assertions.assertFalse(passwordService.isValidPasswordIndex(3, 12345L));
    }

    /**
     * Тест поиска пароля по UUID
     */
    @Test
    void testFindPasswordByUuid() throws PasswordNotFoundException {
        String passUuid = UUID.randomUUID().toString();

        User user = new User(12345L, List.of());
        UserPassword password = new UserPassword(passUuid, "desc", "pass", user, LocalDate.of(2010, 1, 1));

        Mockito.when(userPasswordRepository.findByUuid(passUuid)).thenReturn(password);

        UserPassword expectedPass = passwordService.findPasswordByUuid(passUuid);

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
        String passUuid = UUID.randomUUID().toString();
        User user = new User(12345L, List.of());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user, LocalDate.of(2010, 1, 1));

        String newPass = "newPass";
        String newDesc = "newDesc";
        String encodedPass = "encPass";

        Mockito.when(userPasswordRepository.existsByUuid(passUuid)).thenReturn(true);
        Mockito.when(userPasswordRepository.findByUuid(passUuid)).thenReturn(pass);
        Mockito.when(encodeService.encryptData(newPass)).thenReturn(encodedPass);

        passwordService.updatePassword(passUuid, newDesc, newPass);

        Assertions.assertEquals(newDesc, pass.getDescription());
        Assertions.assertEquals(encodedPass, pass.getPassword());
    }

    /**
     * Тест генерации пароля для сложности 1
     */
    @RepeatedTest(20)
    void testGeneratePassword_WithComplexity_Level1() throws PasswordLengthException, ComplexityFormatException {
        int length = 10;
        String complexity = "1";

        String password = passwordService.generatePassword(length, complexity);

        Assertions.assertEquals(length, password.length());
        Assertions.assertTrue(password.matches("^[a-z]+$"));
    }

    /**
     * Тест генерации пароля для сложности 2
     */
    @RepeatedTest(20)
    void testGeneratePassword_WithComplexity_Level2() throws PasswordLengthException, ComplexityFormatException {
        int length = 12;
        String complexity = "2";

        String password = passwordService.generatePassword(length, complexity);

        Assertions.assertEquals(length, password.length());
        Assertions.assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$"));
    }

    /**
     * Тест генерации пароля для сложности 3
     */
    @RepeatedTest(20)
    void testGeneratePassword_WithComplexity_Level3() throws PasswordLengthException, ComplexityFormatException {
        int length = 15;
        String complexity = "3";

        String password = passwordService.generatePassword(length, complexity);

        Assertions.assertEquals(length, password.length());
        Assertions.assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?])[a-zA-Z\\d!@#$%^&*()\\-_=+<>?]+$"));
    }

    /**
     * Тест уникальности паролей
     */
    @RepeatedTest(5)
    void testGenerateUniquePassword() throws PasswordLengthException, ComplexityFormatException {
        int length = 10;
        String complexity = "2";

        String password1 = passwordService.generatePassword(length, complexity);
        String password2 = passwordService.generatePassword(length, complexity);

        Assertions.assertNotEquals(password1, password2);
    }

    /**
     * Тест генерации паролей при невалидной сложности
     */
    @Test
    void testGeneratePasswordInvalidComplexity() {
        int length = 10;
        String complexity = "4";

        Exception e =  Assertions.assertThrows(ComplexityFormatException.class, () ->
                passwordService.generatePassword(length, complexity));
        Assertions.assertEquals("Complexity should be between 1 and 3", e.getMessage());
    }

    /**
     * Тест генерации паролей при невалидной длине
     */
    @Test
    void testGeneratePasswordInvalidLength() {
        int length = 7;
        String complexity = "3";

        Exception e =  Assertions.assertThrows(PasswordLengthException.class, () ->
                passwordService.generatePassword(length, complexity));
        Assertions.assertEquals("Password length should be between 8 and 128", e.getMessage());
    }
}
