package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.PasswordNotFoundException;
import ru.naumen.handler.*;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;

import java.util.List;
import java.util.Map;

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

    @Mock
    private ValidationService validationService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private GenerateHandler generateHandler;

    @Mock
    private SaveHandler saveHandler;

    @Mock
    private ListHandler listHandler;

    @Mock
    private DeleteHandler deleteHandler;

    @Mock
    private StartHelpHandler startHelpHandler;

    @Mock
    private EditHandler editHandler;

    @Mock
    private NonCommandHandler nonCommandHandler;

    @Mock
    private SortHandler sortHandler;

    @Mock
    private FindHandler findHandler;

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
        Mockito.when(validationService.isValidCommand(new String[]{"/generate", "12", "3"}, 12345L)).thenReturn(true);
        Mockito.when(generateHandler.generatePassword(new String[]{"/generate", "12", "3"}, 12345L))
                .thenReturn(new Response("Сгенерирован пароль: generatedPassword", State.NONE));

        Response response = commandService.performCommand("/generate 12 3", 12345L, "username");
        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /generate, если некорректно задана длина (ниже)
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        Mockito.when(validationService.isValidCommand(new String[]{"/generate", "4", "3"}, 12345L)).thenReturn(true);
        Mockito.when(generateHandler.generatePassword(new String[]{"/generate", "4", "3"}, 12345L))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", State.NONE));
        Response response = commandService.performCommand("/generate 4 3", 12345L, "username");

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /generate, если некорректно задана длина (выше)
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        Mockito.when(validationService.isValidCommand(new String[]{"/generate", "129", "3"}, 12345L)).thenReturn(true);
        Mockito.when(generateHandler.generatePassword(new String[]{"/generate", "129", "3"}, 12345L))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", State.NONE));

        Response response = commandService.performCommand("/generate 129 3", 12345L, "username");
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /generate, если некорректно задана сложность
     */
    @Test
    void testPerformCommandGenerateInvalidComplexity() {
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";
        Mockito.when(validationService.isValidCommand(new String[]{"/generate", "15", "4"}, 12345L)).thenReturn(true);
        Mockito.when(generateHandler.generatePassword(new String[]{"/generate", "15", "4"}, 12345L))
                .thenReturn(new Response(expectedResponse, State.NONE));

        Response response = commandService.performCommand("/generate 15 4", 12345L, "username");
        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        Mockito.when(validationService.isValidCommand(new String[]{"/save", "pass", "desc"}, 12345L)).thenReturn(true);
        Mockito.when(saveHandler.savePassword(new String[]{"/save", "pass", "desc"}, 12345L))
                .thenReturn(new Response("Пароль успешно сохранён", State.NONE));

        Response response = commandService.performCommand("/save pass desc", 12345L, "username");
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        Mockito.when(validationService.isValidCommand(new String[]{"/save", "pass"}, 12345L)).thenReturn(true);
        Mockito.when(saveHandler.savePassword(new String[]{"/save", "pass"}, 12345L))
                .thenReturn(new Response("Пароль успешно сохранён", State.NONE));

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

        String expectedMessage = "\n1) Сайт: desc1, Пароль: dec1\n" +
                "2) Сайт: desc2, Пароль: dec2";

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dec1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dec2");
        Mockito.when(validationService.isValidCommand(new String[]{"/list"}, 12345L)).thenReturn(true);
        Mockito.when(listHandler.getUserPasswords(12345L))
                .thenReturn(new Response(expectedMessage, State.NONE));

        Response response = commandService.performCommand("/list", 12345L, "username");
        Assertions.assertEquals(expectedMessage, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /del, при валидных значениях
     */
    @Test
    void testPerformCommandDelete() {
        Mockito.when(validationService.isValidCommand(new String[]{"/del", "1"}, 12345L)).thenReturn(true);
        Mockito.when(deleteHandler.deletePassword(new String[]{"/del", "1"}, 12345L))
                .thenReturn(new Response("Удалён пароль для сайта site", State.NONE));

        Response response = commandService.performCommand("/del 1", 12345L, "username");
        Assertions.assertEquals("Удалён пароль для сайта site", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /del, если пароль по id не найден
     */
    @Test
    void testPerformCommandDeleteInvalidId() {
        Mockito.when(validationService.isValidCommand(new String[]{"/del", "2"}, 12345L)).thenReturn(true);
        Mockito.when(deleteHandler.deletePassword(new String[]{"/del", "2"}, 12345L))
                .thenReturn(new Response("Не найден пароль с id 2", State.NONE));

        Response response = commandService.performCommand("/del 2", 12345L, "username");
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /del, если задан отрицательный id
     */
    @Test
    void testPerformCommandDeleteMinusId() {
        Mockito.when(validationService.isValidCommand(new String[]{"/del", "-2"}, 12345L)).thenReturn(true);
        Mockito.when(deleteHandler.deletePassword(new String[]{"/del", "-2"}, 12345L))
                .thenReturn(new Response("Не найден пароль с id -2", State.NONE));

        Response response = commandService.performCommand("/del -2", 12345L, "username");
        Assertions.assertEquals("Не найден пароль с id -2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit
     */
    @Test
    void testPerformCommandEditValid() throws PasswordNotFoundException {
        Mockito.when(validationService.isValidCommand(new String[]{"/edit", "1", "12", "2", "updDesc"}, 12345L)).thenReturn(true);
        Mockito.when(editHandler.updatePassword(new String[]{"/edit", "1", "12", "2", "updDesc"}, 12345L))
                .thenReturn(new Response("Обновлён пароль для updDesc: newPass", State.NONE));

        Response response = commandService.performCommand("/edit 1 12 2 updDesc", 12345L, "username");

        Assertions.assertEquals("Обновлён пароль для updDesc: newPass", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit, если не задано описание
     */
    @Test
    void testPerformCommandEditValidWithoutDescription() {
        Mockito.when(validationService.isValidCommand(new String[]{"/edit", "1", "12", "2"}, 12345L)).thenReturn(true);
        Mockito.when(editHandler.updatePassword(new String[]{"/edit", "1", "12", "2"}, 12345L))
                .thenReturn(new Response("Обновлён пароль для site: newPass", State.NONE));

        Response response = commandService.performCommand("/edit 1 12 2", 12345L, "username");

        Assertions.assertEquals("Обновлён пароль для site: newPass", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit, если некорректно задана длина
     */
    @Test
    void testPerformCommandEditInvalidLength() {
        Mockito.when(validationService.isValidCommand(new String[]{"/edit", "1", "129", "2"}, 12345L)).thenReturn(true);
        Mockito.when(editHandler.updatePassword(new String[]{"/edit", "1", "129", "2"}, 12345L))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", State.NONE));

        Response response = commandService.performCommand("/edit 1 129 2", 12345L, "username");
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit, если некорректно задана сложность
     */
    @Test
    void testPerformCommandEditInvalidComplexity() {
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Mockito.when(validationService.isValidCommand(new String[]{"/edit", "1", "12", "4"}, 12345L)).thenReturn(true);
        Mockito.when(editHandler.updatePassword(new String[]{"/edit", "1", "12", "4"}, 12345L))
                .thenReturn(new Response(expectedResponse, State.NONE));

        Response response = commandService.performCommand("/edit 1 12 4", 12345L, "username");

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit, если пароль по id не найден
     */
    @Test
    void testPerformCommandEditPasswordNotFound() {
        Mockito.when(validationService.isValidCommand(new String[]{"/edit", "2", "10", "2"}, 12345L)).thenReturn(true);
        Mockito.when(editHandler.updatePassword(new String[]{"/edit", "2", "10", "2"}, 12345L))
                .thenReturn(new Response("Не найден пароль с id 2", State.NONE));

        Response response = commandService.performCommand("/edit 2 10 2", 12345L, "username");
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit, введённой некорректно
     */
    @Test
    void testPerformCommandEditInvalidCommand() {
        Mockito.when(validationService.isValidCommand(new String[]{"/edit", "2", "10", "2", "15", "14"}, 12345L)).thenReturn(false);

        Response response = commandService.performCommand("/edit 2 10 2 15 14", 12345L, "username");
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }

    /**
     * Тест команды /help
     */
    @Test
    void testPerformCommandHelp() {
        String expectedResponse = "Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.\n\n" +
                "Доступны следующие команды:\n" +
                "- /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);\n" +
                "- /save [password] [description] – Сохранить пароль, задать описание;\n" +
                "- /list – Показать список сохранённых паролей;\n" +
                "- /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;\n" +
                "- /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];\n" +
                "- /help - Справка.";

        Mockito.when(validationService.isValidCommand(new String[]{"/help"}, 12345L)).thenReturn(true);
        Mockito.when(startHelpHandler.helpCommand(12345L))
                .thenReturn(new Response(expectedResponse, State.NONE));

        Response response = commandService.performCommand("/help", 12345L, "username");
        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testPerformCommandInvalidCommand() {
        Mockito.when(validationService.isValidCommand(new String[]{"/invalid", "123"}, 12345L)).thenReturn(false);

        Response response = commandService.performCommand("/invalid 123", 12345L, "username");
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }

    /**
     * Тест, когда генерация должна проходить успешно
     */
    @Test
    void testPerformCommandGenerateKeyboard() {
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(generateHandler.generatePassword(new String[]{"Генерировать"}, 12345L))
                .thenReturn(new Response("Введите длину пароля", State.GENERATION_STEP_1));
        Response firstStep = commandService.performCommand("Генерировать", 12345L, "username");

        Mockito.when(nonCommandHandler.getPasswordLength("20", 12345L, State.GENERATION_STEP_2))
                .thenReturn(new Response("Выберите сложность пароля", State.GENERATION_STEP_2));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.GENERATION_STEP_1));
        Response secondStep = commandService.performCommand("20", 12345L, "username");

        Mockito.when(nonCommandHandler.getComplexity("3", 12345L, State.NONE, null))
                .thenReturn(new Response("Сгенерирован пароль:", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.GENERATION_STEP_2));
        Response thirdStep = commandService.performCommand("3", 12345L, "username");

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_1, firstStep.botState());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_2, secondStep.botState());
        Assertions.assertEquals("Сгенерирован пароль:", thirdStep.message());
        Assertions.assertEquals(State.NONE, thirdStep.botState());
    }

    /**
     * Тест генерации пароля, при неверных параметрах длины
     */
    @Test
    void testPerformCommandGenerateKeyboard_lengthUnCorrect() {
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(generateHandler.generatePassword(new String[]{"Генерировать"}, 12345L))
                .thenReturn(new Response("Введите длину пароля", State.GENERATION_STEP_1));
        Response firstStep = commandService.performCommand("Генерировать", 12345L, "username");

        Mockito.when(nonCommandHandler.getPasswordLength("200", 12345L, State.GENERATION_STEP_2))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.GENERATION_STEP_1));
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
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        String expectedMessage = """
                        Сложность должна быть от 1 до 3, где:
                        1 - простой пароль;
                        2 - пароль средней сложности;
                        3 - сложный пароль.""";

        Mockito.when(generateHandler.generatePassword(new String[]{"Генерировать"}, 12345L))
                .thenReturn(new Response("Введите длину пароля", State.GENERATION_STEP_1));
        Response firstStep = commandService.performCommand("Генерировать", 12345L, "username");

        Mockito.when(nonCommandHandler.getPasswordLength("20", 12345L, State.GENERATION_STEP_2))
                .thenReturn(new Response("Выберите сложность пароля", State.GENERATION_STEP_2));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.GENERATION_STEP_1));
        Response secondStep = commandService.performCommand("20", 12345L, "username");

        Mockito.when(nonCommandHandler.getComplexity("4", 12345L, State.NONE, null))
                .thenReturn(new Response(expectedMessage, State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.GENERATION_STEP_2));
        Response thirdStep = commandService.performCommand("4", 12345L, "username");

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_1, firstStep.botState());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals(State.GENERATION_STEP_2, secondStep.botState());
        Assertions.assertEquals(expectedMessage, thirdStep.message());
        Assertions.assertEquals(State.NONE, thirdStep.botState());
    }

    /**
     * Тест, когда сохранение должно проходить успешно
     */
    @Test
    void testPerformCommandSaveKeyboard() {
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(saveHandler.savePassword(new String[]{"Сохранить"}, 12345L))
                .thenReturn(new Response("Введите пароль", State.SAVE_STEP_1));
        Response firstStep = commandService.performCommand("Сохранить", 12345L, "username");

        Mockito.when(nonCommandHandler.getPassword("password", 12345L, State.SAVE_STEP_2))
                .thenReturn(new Response("Введите описание пароля", State.SAVE_STEP_2));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.SAVE_STEP_1));
        Response secondStep = commandService.performCommand("password", 12345L, "username");

        Mockito.when(nonCommandHandler.getDescription("description", 12345L, State.NONE, null))
                .thenReturn(new Response("Пароль успешно сохранён", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.SAVE_STEP_2));
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
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(editHandler.updatePassword(new String[]{"Изменить"}, 12345L))
                .thenReturn(new Response("Введите индекс пароля", State.EDIT_STEP_1));
        Response firstStep = commandService.performCommand("Изменить", 12345L, "username");

        Mockito.when(nonCommandHandler.getIndexPassword("1", 12345L))
                .thenReturn(new Response("Введите длину пароля", State.EDIT_STEP_2));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.EDIT_STEP_1));
        Response secondStep = commandService.performCommand("1", 12345L, "username");

        Mockito.when(nonCommandHandler.getPasswordLength("20", 12345L, State.EDIT_STEP_3))
                .thenReturn(new Response("Выберите сложность пароля", State.EDIT_STEP_3));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.EDIT_STEP_2));
        Response thirdStep = commandService.performCommand("20", 12345L, "username");

        Mockito.when(nonCommandHandler.getComplexity("2", 12345L, State.EDIT_STEP_4, "Введите описание пароля"))
                .thenReturn(new Response("Введите описание пароля", State.EDIT_STEP_4));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.EDIT_STEP_3));
        Response fourStep = commandService.performCommand("2", 12345L, "username");

        Mockito.when(nonCommandHandler.getDescription("description", 12345L, State.NONE, null))
                .thenReturn(new Response("Обновлён пароль для description", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.EDIT_STEP_4));
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
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(deleteHandler.deletePassword(new String[]{"Удалить"}, 12345L))
                .thenReturn(new Response("Введите индекс пароля", State.DELETE_STEP_1));
        Response firstStep = commandService.performCommand("Удалить", 12345L, "username");

        Mockito.when(nonCommandHandler.getIndexPassword("1", 12345L))
                .thenReturn(new Response("Удалён пароль для сайта description", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.DELETE_STEP_1));
        Response secondStep = commandService.performCommand("1", 12345L, "username");

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals(State.DELETE_STEP_1, firstStep.botState());
        Assertions.assertEquals("Удалён пароль для сайта description", secondStep.message());
        Assertions.assertEquals(State.NONE,secondStep.botState());
    }

    /**
     * Тест команды сортировки
     */
    @Test
    void testPerformCommandSortKeyboard() {
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(sortHandler.sortPasswords(new String[]{"Сортировать"}, 12345L))
                .thenReturn(new Response("Отсортировать по:", State.SORT_STEP_1));
        Response firstStep = commandService.performCommand("Сортировать", 12345L, "username");

        Mockito.when(nonCommandHandler.getSortType("Дате", 12345L))
                .thenReturn(new Response("Отсортированные пароли", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.SORT_STEP_1));
        Response secondStep = commandService.performCommand("Дате", 12345L, "username");

        Assertions.assertEquals("Отсортировать по:", firstStep.message());
        Assertions.assertEquals(State.SORT_STEP_1, firstStep.botState());
        Assertions.assertEquals("Отсортированные пароли", secondStep.message());
        Assertions.assertEquals(State.NONE,secondStep.botState());
    }

    /**
     * Тест команды поиска
     */
    @Test
    void testPerformCommandFindKeyboard() {
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(findHandler.findPasswords(new String[]{"Искать"}, 12345L))
                .thenReturn(new Response("Введите поисковый запрос", State.FIND_STEP_1));
        Response firstStep = commandService.performCommand("Искать", 12345L, "username");

        Mockito.when(nonCommandHandler.getSearchRequest("запрос", 12345L))
                .thenReturn(new Response("Найденные пароли", State.NONE));
        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(Map.of(12345L, State.FIND_STEP_1));
        Response secondStep = commandService.performCommand("запрос", 12345L, "username");

        Assertions.assertEquals("Введите поисковый запрос", firstStep.message());
        Assertions.assertEquals(State.FIND_STEP_1, firstStep.botState());
        Assertions.assertEquals("Найденные пароли", secondStep.message());
        Assertions.assertEquals(State.NONE,secondStep.botState());
    }
}
