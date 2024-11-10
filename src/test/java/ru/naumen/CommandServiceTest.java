package ru.naumen;

import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.service.CommandService;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Класс модульных тестов для CommandService
 */
class CommandServiceTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private EncodeService encodeService;

    @InjectMocks
    private CommandService commandService;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест команды /generate, при валидных значениях
     */
    @Test
    void testPerformCommandGenerate() {
        Mockito.when(passwordService.generatePasswordWithComplexity(12, 3)).thenReturn("generatedPassword");

        String response = commandService.performCommand("/generate 12 3", 12345L);
        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response);
    }

    /**
     * Тест команды /generate, если некорректно задана длина (ниже)
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        String response = commandService.performCommand("/generate 4 3", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);
    }

    /**
     * Тест команды /generate, если некорректно задана длина (выше)
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        String response = commandService.performCommand("/generate 129 3", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);
    }

    /**
     * Тест команды /generate, если некорректно задана сложность
     */
    @Test
    void testPerformCommandGenerateInvalidComplexity() {
        String response = commandService.performCommand("/generate 15 4", 12345L);
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Assertions.assertEquals(expectedResponse, response);
    }

    /**
     * Тест команды /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        String response = commandService.performCommand("/save pass desc", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response);
    }

    /**
     * Тест команды /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        String response = commandService.performCommand("/save pass", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response);
    }

    /**
     * Тест команды /list
     */
    @Test
    void testPerformCommandList() {
        User user = new User("name", 12345L);
        List<UserPassword> userPasswords = List.of(new UserPassword("desc1", "pass1", user),
                new UserPassword("desc2", "pass2", user));

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dec1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dec2");

        String response = commandService.performCommand("/list", 12345L);
        Assertions.assertEquals("\n1) Сайт: desc1, Пароль: dec1\n" +
                "2) Сайт: desc2, Пароль: dec2", response);
    }

    /**
     * Тест команды /del, при валидных значениях
     */
    @Test
    void testPerformCommandDelete() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        String response = commandService.performCommand("/del 1", 12345L);
        Assertions.assertEquals("Удалён пароль для сайта site", response);
        Mockito.verify(passwordService).deletePassword(passUuid);
    }

    /**
     * Тест команды /del, если пароль по id не найден
     */
    @Test
    void testPerformCommandDeleteInvalidId() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        String response = commandService.performCommand("/del 2", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response);
    }

    /**
     * Тест команды /del, если задан отрицательный id
     */
    @Test
    void testPerformCommandDeleteMinusId() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        String response = commandService.performCommand("/del -2", 12345L);
        Assertions.assertEquals("Не найден пароль с id -2", response);
    }

    /**
     * Тест команды /edit
     */
    @Test
    void testPerformCommandEditValid() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.generatePasswordWithComplexity(12, 2)).thenReturn("newPass");
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);

        String response = commandService.performCommand("/edit 1 12 2 updDesc", 12345L);

        Assertions.assertEquals("Обновлён пароль для updDesc: newPass", response);
    }

    /**
     * Тест команды /edit, если не задано описание
     */
    @Test
    void testPerformCommandEditValidWithoutDescription() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.generatePasswordWithComplexity(12, 2)).thenReturn("newPass");
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);

        String response = commandService.performCommand("/edit 1 12 2", 12345L);

        Assertions.assertEquals("Обновлён пароль для site: newPass", response);
    }

    /**
     * Тест команды /edit, если некорректно задана длина
     */
    @Test
    void testPerformCommandEditInvalidLength() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);

        String response = commandService.performCommand("/edit 1 129 2 updDesc", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);
    }

    /**
     * Тест команды /edit, если некорректно задана сложность
     */
    @Test
    void testPerformCommandEditInvalidComplexity() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);

        String response = commandService.performCommand("/edit 1 12 4 updDesc", 12345L);
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Assertions.assertEquals(expectedResponse, response);
    }

    /**
     * Тест команды /edit, если пароль по id не найден
     */
    @Test
    void testPerformCommandEditPasswordNotFound() {
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        String response = commandService.performCommand("/edit 2 10 2 updDesc", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response);
    }

    /**
     * Тест команды /edit, введённой некорректно
     */
    @Test
    void testPerformCommandEditInvalidCommand() {
        String response = commandService.performCommand("/edit 2 10 2 15 14", 12345L);
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response);
    }

    /**
     * Тест команды /help
     */
    @Test
    void testPerformCommandHelp() {
        String response = commandService.performCommand("/help", 12345L);
        String expectedResponse = "Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.\n\n" +
                "Доступны следующие команды:\n" +
                "- /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);\n" +
                "- /save [password] [description] – Сохранить пароль, задать описание;\n" +
                "- /list – Показать список сохранённых паролей;\n" +
                "- /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;\n" +
                "- /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];\n" +
                "- /help - Справка.";

        Assertions.assertEquals(expectedResponse, response);
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testPerformCommandInvalidCommand() {
        String response = commandService.performCommand("/invalid 123", 12345L);
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response);
    }
}
