package ru.naumen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.service.CommandService;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.UserService;

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

    @Mock
    private UserService userService;

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

        Response response = commandService.performCommand("/generate 12 3", 12345L, "username");
        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /generate, если некорректно задана длина (ниже)
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        Response response = commandService.performCommand("/generate 4 3", 12345L, "username");
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /generate, если некорректно задана длина (выше)
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        Response response = commandService.performCommand("/generate 129 3", 12345L, "username");
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /generate, если некорректно задана сложность
     */
    @Test
    void testPerformCommandGenerateInvalidComplexity() {
        Response response = commandService.performCommand("/generate 15 4", 12345L, "username");
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        Response response = commandService.performCommand("/save pass desc", 12345L, "username");
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        Response response = commandService.performCommand("/save pass", 12345L, "username");
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/list", 12345L, "username");
        Assertions.assertEquals("\n1) Сайт: desc1, Пароль: dec1\n" +
                "2) Сайт: desc2, Пароль: dec2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/del 1", 12345L, "username");
        Assertions.assertEquals("Удалён пароль для сайта site", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/del 2", 12345L, "username");
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/del -2", 12345L, "username");
        Assertions.assertEquals("Не найден пароль с id -2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/edit 1 12 2 updDesc", 12345L, "username");

        Assertions.assertEquals("Обновлён пароль для updDesc: newPass", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/edit 1 12 2", 12345L, "username");

        Assertions.assertEquals("Обновлён пароль для site: newPass", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/edit 1 129 2 updDesc", 12345L, "username");
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/edit 1 12 4 updDesc", 12345L, "username");
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
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

        Response response = commandService.performCommand("/edit 2 10 2 updDesc", 12345L, "username");
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit, введённой некорректно
     */
    @Test
    void testPerformCommandEditInvalidCommand() {
        Response response = commandService.performCommand("/edit 2 10 2 15 14", 12345L, "username");
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }

    /**
     * Тест команды /help
     */
    @Test
    void testPerformCommandHelp() {
        Response response = commandService.performCommand("/help", 12345L, "username");
        String expectedResponse = "Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.\n\n" +
                "Доступны следующие команды:\n" +
                "- /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);\n" +
                "- /save [password] [description] – Сохранить пароль, задать описание;\n" +
                "- /list – Показать список сохранённых паролей;\n" +
                "- /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;\n" +
                "- /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];\n" +
                "- /help - Справка.";

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testPerformCommandInvalidCommand() {
        Response response = commandService.performCommand("/invalid 123", 12345L, "username");
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }

    /**
     * Тест, когда генерация должна проходить успешно
     */
    @Test
    void testPerformCommandGenerateKeyboard() {
        Response firstStep = commandService.performCommand("Генерировать", 12345L, "username");
        Response secondStep = commandService.performCommand("20", 12345L, "username");
        Response thirdStep = commandService.performCommand("3", 12345L, "username");

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_1, firstStep.botState());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_2, secondStep.botState());
        Assertions.assertTrue(thirdStep.message().startsWith("Сгенерирован пароль:"));
        Assertions.assertEquals(State.NONE, thirdStep.botState());
    }

    /**
     * Тест генерации пароля, при неверных параметрах длины
     */
    @Test
    void testPerformCommandGenerateKeyboard_lengthUnCorrect() {
        Response firstStep = commandService.performCommand("Генерировать", 12345L, "username");
        Response secondStep = commandService.performCommand("200", 12345L, "username");

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_1, firstStep.botState());
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", secondStep.message());
        Assertions.assertEquals(State.NONE, secondStep.botState());
    }

    /**
     * Тест генерации пароля, при неверных параметрах сложности
     */
    @Test
    void testPerformCommandGenerateKeyboard_ComplexityUnCorrect() {
        Response firstStep = commandService.performCommand("Генерировать", 12345L, "username");
        Response secondStep = commandService.performCommand("20", 12345L, "username");
        Response thirdStep = commandService.performCommand("4", 12345L, "username");

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_1, firstStep.botState());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_2, secondStep.botState());
        Assertions.assertEquals(
                """
                        Сложность должна быть от 1 до 3, где:
                        1 - простой пароль;
                        2 - пароль средней сложности;
                        3 - сложный пароль.""", thirdStep.message());
        Assertions.assertEquals(State.NONE, thirdStep.botState());
    }

    /**
     * Тест, когда сохранение должно проходить успешно
     */
    @Test
    void testPerformCommandSaveKeyboard() {
        Response firstStep = commandService.performCommand("Сохранить", 12345L, "username");
        Response secondStep = commandService.performCommand("password", 12345L, "username");
        Response thirdStep = commandService.performCommand("description", 12345L, "username");

        Assertions.assertEquals("Введите пароль", firstStep.message());
        Assertions.assertEquals(State.SAVE_STEP_1, firstStep.botState());
        Assertions.assertEquals("Введите описание пароля", secondStep.message());
        Assertions.assertEquals(State.SAVE_STEP_2, secondStep.botState());
        Assertions.assertEquals("Пароль успешно сохранён", thirdStep.message());
        Assertions.assertEquals(State.NONE, thirdStep.botState());
    }

    /**
     * Тест, когда изменение должно проходить успешно
     */
    @Test
    void testPerformCommandEditKeyboard() {
        User user = new User("userUuid", "name", 12345L, new ArrayList<>());
        UserPassword password = new UserPassword("uuid", "description", "pass", user);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(List.of(password));
        Mockito.when(passwordService.findPasswordByUuid("uuid")).thenReturn(password);

        Response firstStep = commandService.performCommand("Изменить", 12345L, "username");
        Response secondStep = commandService.performCommand("1", 12345L, "username");
        Response thirdStep = commandService.performCommand("20", 12345L, "username");
        Response fourStep = commandService.performCommand("2", 12345L, "username");
        Response fiveStep = commandService.performCommand("description", 12345L, "username");

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals(State.EDIT_STEP_1, firstStep.botState());
        Assertions.assertEquals("Введите длину пароля", secondStep.message());
        Assertions.assertEquals(State.EDIT_STEP_2, secondStep.botState());
        Assertions.assertEquals("Выберите сложность пароля", thirdStep.message());
        Assertions.assertEquals(State.EDIT_STEP_3, thirdStep.botState());
        Assertions.assertEquals("Введите описание пароля", fourStep.message());
        Assertions.assertEquals(State.EDIT_STEP_4, fourStep.botState());
        Assertions.assertTrue(fiveStep.message().startsWith("Обновлён пароль для description"));
        Assertions.assertEquals(State.NONE, fiveStep.botState());
    }

    /**
     * Тест, когда удаление должно проходить успешно
     */
    @Test
    void testPerformCommandDeleteKeyboard() {
        User user = new User("userUuid", "name", 12345L, new ArrayList<>());
        UserPassword password = new UserPassword("uuid", "description", "pass", user);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(List.of(password));
        Mockito.when(passwordService.findPasswordByUuid("uuid")).thenReturn(password);

        Response firstStep = commandService.performCommand("Удалить", 12345L, "username");
        Response secondStep = commandService.performCommand("1", 12345L, "username");

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals(State.DELETE_STEP_1, firstStep.botState());
        Assertions.assertEquals("Удалён пароль для сайта description", secondStep.message());
        Assertions.assertEquals(State.NONE,secondStep.botState());
        Mockito.verify(passwordService).deletePassword("uuid");
    }
}
