package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.keyboard.Keyboard;
import ru.naumen.bot.Response;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.cache.UserStateCache;
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
    private UserStateCache userStateCache;

    @Mock
    private NonCommandHandler nonCommandHandler;

    @Mock
    private GenerateHandler generateHandler;

    @Mock
    private SaveHandler saveHandler;

    @Mock
    private ListHandler listHandler;

    @Mock
    private DeleteHandler deleteHandler;

    @Mock
    private EditHandler editHandler;

    @Mock
    private SortHandler sortHandler;

    @Mock
    private FindHandler findHandler;

    @Mock
    private HelpHandler helpHandler;

    @Mock
    private KeyboardCreator keyboardCreator;

    @Mock
    private RemindHandler remindHandler;
    @Mock
    private ClearPasswordHandler clearPasswordHandler;
    @Mock
    private AddCodePhraseHandler addCodePhraseHandler;

    private CommandService commandService;

    /**
     * Перед каждым тестом создаёт объекты (не моки!)
     * класса поиска команд, отображения хэндлеров и тестируемого класса
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Map<String, CommandHandler> commandHandlers = Map.of(
                "/generate", generateHandler,
                "/list", listHandler,
                "/edit", editHandler,
                "/del", deleteHandler,
                "/save", saveHandler,
                "/sort", sortHandler,
                "/find", findHandler,
                "/help", helpHandler
        );

        commandService = new CommandService(
                userStateCache,
                nonCommandHandler,
                keyboardCreator,
                commandHandlers
        );
    }

    /**
     * Тест команды /generate, при валидных значениях
     */
    @Test
    void testPerformCommandGenerate() {
        Mockito.when(generateHandler.handle(new String[]{"/generate", "12", "3"}, 12345L))
                .thenReturn(new Response("Сгенерирован пароль: generatedPassword",
                        new Keyboard(List.of())));

        Response response = commandService.performCommand("/generate 12 3", 12345L);
        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response.message());
    }

    /**
     * Тест команды /generate, если некорректно задана длина (ниже)
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        Mockito.when(generateHandler.handle(new String[]{"/generate", "4", "3"}, 12345L))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!",
                        new Keyboard(List.of())));
        Response response = commandService.performCommand("/generate 4 3", 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
    }

    /**
     * Тест команды /generate, если некорректно задана длина (выше)
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        Mockito.when(generateHandler.handle(new String[]{"/generate", "129", "3"}, 12345L))
                .thenReturn(
                        new Response("Длина пароля должна быть от 8 до 128 символов!",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/generate 129 3", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
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
        Mockito.when(generateHandler.handle(new String[]{"/generate", "15", "4"}, 12345L))
                .thenReturn(
                        new Response(expectedResponse,
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/generate 15 4", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        Mockito.when(saveHandler.handle(new String[]{"/save", "pass", "desc"}, 12345L))
                .thenReturn(
                        new Response("Пароль успешно сохранён",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/save pass desc", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
    }

    /**
     * Тест команды /save с установкой напоминания
     */
    @Test
    void testPerformCommandSaveWithRemind() {
        Mockito.when(validationService.isValidCommand(new String[]{"/save", "pass", "desc", "3"}, 12345L)).thenReturn(true);
        Mockito.when(saveHandler.handle(new String[]{"/save", "pass", "desc", "3"}, 12345L))
                .thenReturn(new Response("Пароль успешно сохранён", State.NONE));

        Response response = commandService.performCommand("/save pass desc 3", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        Mockito.when(saveHandler.handle(new String[]{"/save", "pass"}, 12345L))
                .thenReturn(
                        new Response("Пароль успешно сохранён",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/save pass", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
    }

    /**
     * Тест команды /list
     */
    @Test
    void testPerformCommandList() {
        User user = new User(12345L);
        List<UserPassword> userPasswords = List.of(new UserPassword("desc1", "pass1", user),
                new UserPassword("desc2", "pass2", user));

        String expectedMessage = "\n1) Сайт: desc1, Пароль: dec1\n" +
                "2) Сайт: desc2, Пароль: dec2";

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dec1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dec2");
        Mockito.when(listHandler.handle(new String[]{"/list"}, 12345L))
                .thenReturn(
                        new Response(expectedMessage,
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/list", 12345L);
        Assertions.assertEquals(expectedMessage, response.message());
    }

    /**
     * Тест команды /del, при валидных значениях
     */
    @Test
    void testPerformCommandDelete() {
        Mockito.when(deleteHandler.handle(new String[]{"/del", "1"}, 12345L))
                .thenReturn(
                        new Response("Удалён пароль для сайта site",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/del 1", 12345L);
        Assertions.assertEquals("Удалён пароль для сайта site", response.message());
    }

    /**
     * Тест команды /del, если пароль по id не найден
     */
    @Test
    void testPerformCommandDeleteInvalidId() {
        Mockito.when(deleteHandler.handle(new String[]{"/del", "2"}, 12345L))
                .thenReturn(
                        new Response("Не найден пароль с id 2",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/del 2", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
    }

    /**
     * Тест команды /del, если задан отрицательный id
     */
    @Test
    void testPerformCommandDeleteMinusId() {
        Mockito.when(deleteHandler.handle(new String[]{"/del", "-2"}, 12345L))
                .thenReturn(
                        new Response("Не найден пароль с id -2",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/del -2", 12345L);
        Assertions.assertEquals("Не найден пароль с id -2", response.message());
    }

    /**
     * Тест команды /remind, при валидных значениях
     */
    @Test
    void testPerformCommandRemind() {
        Mockito.when(validationService.isValidCommand(new String[]{"/remind", "1", "10"}, 12345L)).thenReturn(true);
        Mockito.when(remindHandler.handle(new String[]{"/remind", "1", "10"}, 12345L))
                .thenReturn(new Response("Установлено напоминание", State.NONE));

        Response response = commandService.performCommand("/remind 1 10", 12345L);
        Assertions.assertEquals("Установлено напоминание", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /remind, если задан отрицательный id
     */
    @Test
    void testPerformCommandRemindMinusId() {
        Mockito.when(validationService.isValidCommand(new String[]{"/remind", "2", "10"}, 12345L)).thenReturn(true);
        Mockito.when(remindHandler.handle(new String[]{"/remind", "2", "10"}, 12345L))
                .thenReturn(new Response("Не найден пароль с id 2", State.NONE));

        Response response = commandService.performCommand("/remind 2 10", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /remind, если задано некорректное кол-во дней
     */
    @Test
    void testPerformCommandRemindInvalidDays() {
        Mockito.when(validationService.isValidCommand(new String[]{"/remind", "1", "1"}, 12345L)).thenReturn(true);
        Mockito.when(remindHandler.handle(new String[]{"/remind", "1", "1"}, 12345L))
                .thenReturn(new Response("Напоминание можно установить на срок от 3 до 90 дней", State.NONE));

        Response response = commandService.performCommand("/remind 1 1", 12345L);
        Assertions.assertEquals("Напоминание можно установить на срок от 3 до 90 дней", response.message());
        Assertions.assertEquals(State.NONE, response.botState());
    }

    /**
     * Тест команды /edit
     */
    @Test
    void testPerformCommandEditValid() {
        Mockito.when(editHandler.handle(new String[]{"/edit", "1", "12", "2", "updDesc"}, 12345L))
                .thenReturn(
                        new Response("Обновлён пароль для updDesc: newPass",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/edit 1 12 2 updDesc", 12345L);

        Assertions.assertEquals("Обновлён пароль для updDesc: newPass", response.message());
    }

    /**
     * Тест команды /edit, если не задано описание
     */
    @Test
    void testPerformCommandEditValidWithoutDescription() {
        Mockito.when(editHandler.handle(new String[]{"/edit", "1", "12", "2"}, 12345L))
                .thenReturn(
                        new Response("Обновлён пароль для site: newPass",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/edit 1 12 2", 12345L);

        Assertions.assertEquals("Обновлён пароль для site: newPass", response.message());
    }

    /**
     * Тест команды /edit, если некорректно задана длина
     */
    @Test
    void testPerformCommandEditInvalidLength() {
        Mockito.when(editHandler.handle(new String[]{"/edit", "1", "129", "2"}, 12345L))
                .thenReturn(
                        new Response("Длина пароля должна быть от 8 до 128 символов!",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/edit 1 129 2", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
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

        Mockito.when(editHandler.handle(new String[]{"/edit", "1", "12", "4"}, 12345L))
                .thenReturn(
                        new Response(expectedResponse,
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/edit 1 12 4", 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды /edit, если пароль по id не найден
     */
    @Test
    void testPerformCommandEditPasswordNotFound() {
        Mockito.when(editHandler.handle(new String[]{"/edit", "2", "10", "2"}, 12345L))
                .thenReturn(
                        new Response("Не найден пароль с id 2",
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/edit 2 10 2", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
    }

    /**
     * Тест команды /edit, введённой некорректно
     */
    @Test
    void testPerformCommandEditInvalidCommand() {

        Response response = commandService.performCommand("/edit 2 10 2 15 14", 12345L);
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

        Mockito.when(helpHandler.handle(new String[]{"/help"}, 12345L))
                .thenReturn(
                        new Response(expectedResponse,
                                new Keyboard(List.of())
                        ));

        Response response = commandService.performCommand("/help", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testPerformCommandInvalidCommand() {

        Response response = commandService.performCommand("/invalid 123", 12345L);
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }

    /**
     * Тест, когда генерация должна проходить успешно
     */
    @Test
    void testPerformCommandGenerateKeyboard() {

        Mockito.when(generateHandler.handle(new String[]{"Генерировать"}, 12345L))
                .thenReturn(
                        new Response("Введите длину пароля",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Генерировать", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength("20", 12345L, State.GENERATION_STEP_2))
                .thenReturn(
                        new Response("Выберите сложность пароля",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);
        Response secondStep = commandService.performCommand("20", 12345L);

        Mockito.when(nonCommandHandler.getComplexity("3", 12345L, State.NONE, null))
                .thenReturn(
                        new Response("Сгенерирован пароль:",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_2);
        Response thirdStep = commandService.performCommand("3", 12345L);

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals("Сгенерирован пароль:", thirdStep.message());
    }

    /**
     * Тест генерации пароля, при неверных параметрах длины
     */
    @Test
    void testPerformCommandGenerateKeyboard_lengthUnCorrect() {
        Mockito.when(generateHandler.handle(new String[]{"Генерировать"}, 12345L))
                .thenReturn(
                        new Response("Введите длину пароля",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Генерировать", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength("200", 12345L, State.GENERATION_STEP_2))
                .thenReturn(
                        new Response("Длина пароля должна быть от 8 до 128 символов!",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);
        Response secondStep = commandService.performCommand("200", 12345L);

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", secondStep.message());
    }

    /**
     * Тест генерации пароля, при неверных параметрах сложности
     */
    @Test
    void testPerformCommandGenerateKeyboard_ComplexityUnCorrect() {
        String expectedMessage = """
                Сложность должна быть от 1 до 3, где:
                1 - простой пароль;
                2 - пароль средней сложности;
                3 - сложный пароль.""";

        Mockito.when(generateHandler.handle(new String[]{"Генерировать"}, 12345L))
                .thenReturn(
                        new Response("Введите длину пароля",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Генерировать", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength("20", 12345L, State.GENERATION_STEP_2))
                .thenReturn(
                        new Response("Выберите сложность пароля",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);
        Response secondStep = commandService.performCommand("20", 12345L);

        Mockito.when(nonCommandHandler.getComplexity("4", 12345L, State.NONE, null))
                .thenReturn(
                        new Response(expectedMessage,
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_2);
        Response thirdStep = commandService.performCommand("4", 12345L);

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals(expectedMessage, thirdStep.message());
    }

    /**
     * Тест, когда сохранение должно проходить успешно
     */
    @Test
    void testPerformCommandSaveKeyboard() {
        Mockito.when(saveHandler.handle(new String[]{"Сохранить"}, 12345L))
                .thenReturn(
                        new Response("Введите пароль",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Сохранить", 12345L);

        Mockito.when(nonCommandHandler.getPassword("password", 12345L, State.SAVE_STEP_2))
                .thenReturn(
                        new Response("Введите описание пароля",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SAVE_STEP_1);
        Response secondStep = commandService.performCommand("password", 12345L);

        Mockito.when(nonCommandHandler.getDescription("description", 12345L, State.NONE, null))
                .thenReturn(
                        new Response("Пароль успешно сохранён",
                                new Keyboard(List.of())
                        ));
        Mockito.when(nonCommandHandler.getDescription("description", 12345L, State.SAVE_STEP_3, null))
                .thenReturn(new Response("Установить напоминание о смене пароля? Стандартное значение 30 дней, сохранить?", State.SAVE_STEP_3));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SAVE_STEP_2);
        Response thirdStep = commandService.performCommand("description", 12345L);

        Mockito.when(nonCommandHandler.getAgreement("Нет", 12345L))
                .thenReturn(new Response("Через сколько дней напомнить о смене пароля? (0 - не ставить напоминание)", State.SAVE_STEP_4));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SAVE_STEP_3);
        Response fourthStep = commandService.performCommand("Нет", 12345L);

        Mockito.when(nonCommandHandler.getRemindDays("3", 12345L, State.NONE))
                .thenReturn(new Response("Пароль успешно сохранён", State.NONE));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SAVE_STEP_4);
        Response fifthStep = commandService.performCommand("3", 12345L);


        Assertions.assertEquals("Введите пароль", firstStep.message());
        Assertions.assertEquals("Введите описание пароля", secondStep.message());
        Assertions.assertEquals("Пароль успешно сохранён", thirdStep.message());
        Assertions.assertEquals(State.SAVE_STEP_2, secondStep.botState());
        Assertions.assertEquals("Установить напоминание о смене пароля? Стандартное значение 30 дней, сохранить?", thirdStep.message());
        Assertions.assertEquals(State.SAVE_STEP_3, thirdStep.botState());
        Assertions.assertEquals("Через сколько дней напомнить о смене пароля? (0 - не ставить напоминание)", fourthStep.message());
        Assertions.assertEquals(State.SAVE_STEP_4, fourthStep.botState());
        Assertions.assertEquals("Пароль успешно сохранён", fifthStep.message());
        Assertions.assertEquals(State.NONE, fifthStep.botState());
    }

    /**
     * Тест, когда изменение должно проходить успешно
     */
    @Test
    void testPerformCommandEditKeyboard() {
        Mockito.when(editHandler.handle(new String[]{"Изменить"}, 12345L))
                .thenReturn(
                        new Response("Введите индекс пароля",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Изменить", 12345L);

        Mockito.when(nonCommandHandler.getIndexPassword("1", 12345L))
                .thenReturn(
                        new Response("Введите длину пароля",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.EDIT_STEP_1);
        Response secondStep = commandService.performCommand("1", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength("20", 12345L, State.EDIT_STEP_3))
                .thenReturn(
                        new Response("Выберите сложность пароля",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.EDIT_STEP_2);
        Response thirdStep = commandService.performCommand("20", 12345L);

        Mockito.when(nonCommandHandler.getComplexity("2", 12345L, State.EDIT_STEP_4, "Введите описание пароля"))
                .thenReturn(
                        new Response("Введите описание пароля",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.EDIT_STEP_3);
        Response fourStep = commandService.performCommand("2", 12345L);

        Mockito.when(nonCommandHandler.getDescription("description", 12345L, State.NONE, null))
                .thenReturn(
                        new Response("Обновлён пароль для description",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.EDIT_STEP_4);
        Response fiveStep = commandService.performCommand("description", 12345L);

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals("Введите длину пароля", secondStep.message());
        Assertions.assertEquals("Выберите сложность пароля", thirdStep.message());
        Assertions.assertEquals("Введите описание пароля", fourStep.message());
        Assertions.assertTrue(fiveStep.message().startsWith("Обновлён пароль для description"));
    }

    /**
     * Тест, когда удаление должно проходить успешно
     */
    @Test
    void testPerformCommandDeleteKeyboard() {
        Mockito.when(deleteHandler.handle(new String[]{"Удалить"}, 12345L))
                .thenReturn(
                        new Response("Введите индекс пароля",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Удалить", 12345L);

        Mockito.when(nonCommandHandler.getIndexPassword("1", 12345L))
                .thenReturn(
                        new Response("Удалён пароль для сайта description",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.DELETE_STEP_1);
        Response secondStep = commandService.performCommand("1", 12345L);

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals("Удалён пароль для сайта description", secondStep.message());
    }

    /**
     * Тест команды сортировки
     */
    @Test
    void testPerformCommandSortKeyboard() {
        Mockito.when(sortHandler.handle(new String[]{"Сортировать"}, 12345L))
                .thenReturn(
                        new Response("Отсортировать по:",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Сортировать", 12345L);

        Mockito.when(nonCommandHandler.getSortType("Дате", 12345L))
                .thenReturn(
                        new Response("Отсортированные пароли",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SORT_STEP_1);
        Response secondStep = commandService.performCommand("Дате", 12345L);

        Assertions.assertEquals("Отсортировать по:", firstStep.message());
        Assertions.assertEquals("Отсортированные пароли", secondStep.message());
    }

    /**
     * Тест команды поиска
     */
    @Test
    void testPerformCommandFindKeyboard() {
        Mockito.when(findHandler.handle(new String[]{"Искать"}, 12345L))
                .thenReturn(
                        new Response("Введите поисковый запрос",
                                new Keyboard(List.of())
                        ));
        Response firstStep = commandService.performCommand("Искать", 12345L);

        Mockito.when(nonCommandHandler.getSearchRequest("запрос", 12345L))
                .thenReturn(
                        new Response("Найденные пароли",
                                new Keyboard(List.of())
                        ));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.FIND_STEP_1);
        Response secondStep = commandService.performCommand("запрос", 12345L);

        Assertions.assertEquals("Введите поисковый запрос", firstStep.message());
        Assertions.assertEquals("Найденные пароли", secondStep.message());
    }

    /**
     * Тест команды установки напоминания
     */
    @Test
    void testPerformCommandRemindKeyboard() {
        Mockito.when(validationService.isValidCommand(Mockito.any(String[].class), Mockito.eq(12345L))).thenReturn(true);

        Mockito.when(remindHandler.handle(new String[]{"Напомнить"}, 12345L))
                .thenReturn(new Response("Введите индекс пароля", State.REMIND_STEP_1));
        Response firstStep = commandService.performCommand("Напомнить", 12345L);

        Mockito.when(nonCommandHandler.getIndexPassword("1", 12345L))
                .thenReturn(new Response("Через сколько дней напомнить о смене пароля?", State.REMIND_STEP_2));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.REMIND_STEP_1);
        Response secondStep = commandService.performCommand("1", 12345L);

        Mockito.when(nonCommandHandler.getRemindDays("3", 12345L, State.NONE))
                .thenReturn(new Response("Напоминание установлено", State.NONE));
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.REMIND_STEP_2);
        Response thirdStep = commandService.performCommand("3", 12345L);

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals(State.REMIND_STEP_1, firstStep.botState());
        Assertions.assertEquals("Через сколько дней напомнить о смене пароля?", secondStep.message());
        Assertions.assertEquals(State.REMIND_STEP_2, secondStep.botState());
        Assertions.assertEquals("Напоминание установлено", thirdStep.message());
        Assertions.assertEquals(State.NONE, thirdStep.botState());
    }
}
