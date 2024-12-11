package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.handler.*;
import ru.naumen.keyboard.Keyboard;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;

import java.util.List;
import java.util.Map;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Map<String, CommandHandler> commandHandlers = Map.ofEntries(
                Map.entry("/generate", generateHandler),
                Map.entry("/list", listHandler),
                Map.entry("/edit", editHandler),
                Map.entry("/del", deleteHandler),
                Map.entry("/save", saveHandler),
                Map.entry("/sort", sortHandler),
                Map.entry("/find", findHandler),
                Map.entry("/help", helpHandler),
                Map.entry("/remind", remindHandler),
                Map.entry("/code", addCodePhraseHandler),
                Map.entry("/clear", clearPasswordHandler)
        );

        commandService = new CommandService(
                userStateCache,
                nonCommandHandler,
                keyboardCreator,
                commandHandlers
        );
    }

    /**
     * Тест команды /generate
     */
    @Test
    void testPerformCommandGenerate() {
        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"/generate", "12", "3"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Сгенерирован пароль: generatedPassword", new Keyboard(List.of())));

        Response response = commandService.performCommand("/generate 12 3", 12345L);
        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response.message());
    }

    /**
     * Тест команды /generate при недостаточной длине
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"/generate", "4", "3"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", new Keyboard(List.of())));
        Response response = commandService.performCommand("/generate 4 3", 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
    }

    /**
     * Тест команды /generate при высокой длине
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"/generate", "129", "3"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", new Keyboard(List.of())));

        Response response = commandService.performCommand("/generate 129 3", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
    }

    /**
     * Тест команды /generate при некорректной сложности
     */
    @Test
    void testPerformCommandGenerateInvalidComplexity() {
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";
        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"/generate", "15", "4"}), Mockito.eq(12345L)))
                .thenReturn(new Response(expectedResponse, new Keyboard(List.of())));

        Response response = commandService.performCommand("/generate 15 4", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        Mockito.when(saveHandler.handle(Mockito.eq(new String[]{"/save", "pass", "desc"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Пароль успешно сохранён", new Keyboard(List.of())));

        Response response = commandService.performCommand("/save pass desc", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
    }

    /**
     * Тест команды /save с напоминанием
     */
    @Test
    void testPerformCommandSaveWithRemind() {
        Mockito.when(saveHandler.handle(Mockito.eq(new String[]{"/save", "pass", "desc", "3"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Пароль успешно сохранён", new Keyboard(List.of())));

        Response response = commandService.performCommand("/save pass desc 3", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
    }

    /**
     * Тест команды /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        Mockito.when(saveHandler.handle(Mockito.eq(new String[]{"/save", "pass"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Пароль успешно сохранён", new Keyboard(List.of())));

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

        Mockito.when(passwordService.getUserPasswords(Mockito.eq(12345L))).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dec1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dec2");
        Mockito.when(listHandler.handle(Mockito.eq(new String[]{"/list"}), Mockito.eq(12345L)))
                .thenReturn(new Response(expectedMessage, new Keyboard(List.of())));

        Response response = commandService.performCommand("/list", 12345L);
        Assertions.assertEquals(expectedMessage, response.message());
    }

    /**
     * Тест команды /del
     */
    @Test
    void testPerformCommandDelete() {
        Mockito.when(deleteHandler.handle(Mockito.eq(new String[]{"/del", "1"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Удалён пароль для сайта site", new Keyboard(List.of())));

        Response response = commandService.performCommand("/del 1", 12345L);
        Assertions.assertEquals("Удалён пароль для сайта site", response.message());
    }

    /**
     * Тест команды /delete с некрректным индексом
     */
    @Test
    void testPerformCommandDeleteInvalidIndex() {
        Mockito.when(deleteHandler.handle(Mockito.eq(new String[]{"/del", "2"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Не найден пароль с id 2", new Keyboard(List.of())));

        Response response = commandService.performCommand("/del 2", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
    }

    /**
     * Тест команды /delete с отрицательным индексом
     */
    @Test
    void testPerformCommandDeleteMinusIndex() {
        Mockito.when(deleteHandler.handle(Mockito.eq(new String[]{"/del", "-2"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Не найден пароль с id -2", new Keyboard(List.of())));

        Response response = commandService.performCommand("/del -2", 12345L);
        Assertions.assertEquals("Не найден пароль с id -2", response.message());
    }

    /**
     * Тест команды /remind
     */
    @Test
    void testPerformCommandRemind() {
        Mockito.when(remindHandler.handle(Mockito.eq(new String[]{"/remind", "1", "10"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Установлено напоминание", new Keyboard(List.of())));

        Response response = commandService.performCommand("/remind 1 10", 12345L);
        Assertions.assertEquals("Установлено напоминание", response.message());
    }

    /**
     * Тест команды /remind с отрицательным индексом
     */
    @Test
    void testPerformCommandRemindMinusIndex() {
        Mockito.when(remindHandler.handle(Mockito.eq(new String[]{"/remind", "2", "10"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Не найден пароль с id 2", new Keyboard(List.of())));

        Response response = commandService.performCommand("/remind 2 10", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
    }

    /**
     * Тест команды /remind с невалидным кол-вом дней
     */
    @Test
    void testPerformCommandRemindInvalidDays() {
        Mockito.when(remindHandler.handle(Mockito.eq(new String[]{"/remind", "1", "1"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Напоминание можно установить на срок от 3 до 90 дней", new Keyboard(List.of())));

        Response response = commandService.performCommand("/remind 1 1", 12345L);
        Assertions.assertEquals("Напоминание можно установить на срок от 3 до 90 дней", response.message());
    }

    /**
     * Тест команды /edit
     */
    @Test
    void testPerformCommandEditValid() {
        Mockito.when(editHandler.handle(Mockito.eq(new String[]{"/edit", "1", "12", "2", "updDesc"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Обновлён пароль для updDesc: newPass", new Keyboard(List.of())));

        Response response = commandService.performCommand("/edit 1 12 2 updDesc", 12345L);
        Assertions.assertEquals("Обновлён пароль для updDesc: newPass", response.message());
    }

    /**
     * Тест команды /edit без описания
     */
    @Test
    void testPerformCommandEditValidWithoutDescription() {
        Mockito.when(editHandler.handle(Mockito.eq(new String[]{"/edit", "1", "12", "2"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Обновлён пароль для site: newPass", new Keyboard(List.of())));

        Response response = commandService.performCommand("/edit 1 12 2", 12345L);
        Assertions.assertEquals("Обновлён пароль для site: newPass", response.message());
    }

    /**
     * Тест команды /edit при невалидной длине
     */
    @Test
    void testPerformCommandEditInvalidLength() {
        Mockito.when(editHandler.handle(Mockito.eq(new String[]{"/edit", "1", "129", "2"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", new Keyboard(List.of())));

        Response response = commandService.performCommand("/edit 1 129 2", 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
    }

    /**
     * Тест команды /edit при невалидной сложности
     */
    @Test
    void testPerformCommandEditInvalidComplexity() {
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Mockito.when(editHandler.handle(Mockito.eq(new String[]{"/edit", "1", "12", "4"}), Mockito.eq(12345L)))
                .thenReturn(new Response(expectedResponse, new Keyboard(List.of())));

        Response response = commandService.performCommand("/edit 1 12 4", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды /edit при не найденном пароле
     */
    @Test
    void testPerformCommandEditPasswordNotFound() {
        Mockito.when(editHandler.handle(Mockito.eq(new String[]{"/edit", "2", "10", "2"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Не найден пароль с id 2", new Keyboard(List.of())));

        Response response = commandService.performCommand("/edit 2 10 2", 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response.message());
    }

    /**
     * Тест команды /edit при невалидном кол-ве параметров
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

        Mockito.when(helpHandler.handle(Mockito.eq(new String[]{"/help"}), Mockito.eq(12345L)))
                .thenReturn(new Response(expectedResponse, new Keyboard(List.of())));

        Response response = commandService.performCommand("/help", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды /code
     */
    @Test
    void testPerformCommandCode() {
        String expectedResponse = "Кодовое слово успешно установлено";

        Mockito.when(addCodePhraseHandler.handle(Mockito.eq(new String[]{"/code", "code"}), Mockito.eq(12345L)))
                .thenReturn(new Response(expectedResponse, new Keyboard(List.of())));

        Response response = commandService.performCommand("/code code", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды /clear
     */
    @Test
    void testPerformCommandClear() {
        String expectedResponse = "Удалён 1 пароль";

        Mockito.when(clearPasswordHandler.handle(Mockito.eq(new String[]{"/clear", "code", "start"}), Mockito.eq(12345L)))
                .thenReturn(new Response(expectedResponse, new Keyboard(List.of())));

        Response response = commandService.performCommand("/clear code start", 12345L);
        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест команды некорректной
     */
    @Test
    void testPerformCommandInvalidCommand() {
        Response response = commandService.performCommand("/invalid 123", 12345L);
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }

    /**
     * Тест команды /generate с клавиатуры
     */
    @Test
    void testPerformCommandGenerateKeyboard() {
        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"Генерировать"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Введите длину пароля", new Keyboard(List.of())));
        Response firstStep = commandService.performCommand("Генерировать", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength(Mockito.eq("20"), Mockito.eq(12345L), Mockito.eq(State.GENERATION_STEP_2)))
                .thenReturn(new Response("Выберите сложность пароля", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.GENERATION_STEP_1);
        Response secondStep = commandService.performCommand("20", 12345L);

        Mockito.when(nonCommandHandler.getComplexity(Mockito.eq("3"), Mockito.eq(12345L), Mockito.eq(State.NONE), Mockito.isNull()))
                .thenReturn(new Response("Сгенерирован пароль:", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.GENERATION_STEP_2);
        Response thirdStep = commandService.performCommand("3", 12345L);

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals("Сгенерирован пароль:", thirdStep.message());
    }

    /**
     * Тест команды /generate с клавиатуры при некорректной длине
     */
    @Test
    void testPerformCommandGenerateKeyboard_lengthUnCorrect() {
        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"Генерировать"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Введите длину пароля", new Keyboard(List.of())));
        Response firstStep = commandService.performCommand("Генерировать", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength(Mockito.eq("200"), Mockito.eq(12345L), Mockito.eq(State.GENERATION_STEP_2)))
                .thenReturn(new Response("Длина пароля должна быть от 8 до 128 символов!", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.GENERATION_STEP_1);
        Response secondStep = commandService.performCommand("200", 12345L);

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", secondStep.message());
    }

    /**
     * Тест команды /generate с клавиатуры при некорректной сложности
     */
    @Test
    void testPerformCommandGenerateKeyboard_ComplexityUnCorrect() {
        String expectedMessage = """
                Сложность должна быть от 1 до 3, где:
                1 - простой пароль;
                2 - пароль средней сложности;
                3 - сложный пароль.""";

        Mockito.when(generateHandler.handle(Mockito.eq(new String[]{"Генерировать"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Введите длину пароля", new Keyboard(List.of())));
        Response firstStep = commandService.performCommand("Генерировать", 12345L);

        Mockito.when(nonCommandHandler.getPasswordLength(Mockito.eq("20"), Mockito.eq(12345L), Mockito.eq(State.GENERATION_STEP_2)))
                .thenReturn(new Response("Выберите сложность пароля", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.GENERATION_STEP_1);
        Response secondStep = commandService.performCommand("20", 12345L);

        Mockito.when(nonCommandHandler.getComplexity(Mockito.eq("4"), Mockito.eq(12345L), Mockito.eq(State.NONE), Mockito.isNull()))
                .thenReturn(new Response(expectedMessage, new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.GENERATION_STEP_2);
        Response thirdStep = commandService.performCommand("4", 12345L);

        Assertions.assertEquals("Введите длину пароля", firstStep.message());
        Assertions.assertEquals("Выберите сложность пароля", secondStep.message());
        Assertions.assertEquals(expectedMessage, thirdStep.message());
    }

    /**
     * Тест команды /save с клавиатуры
     */
    @Test
    void testPerformCommandSaveKeyboard() {
        Mockito.when(saveHandler.handle(Mockito.eq(new String[]{"Сохранить"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Введите пароль", new Keyboard(List.of())));
        Response firstStep = commandService.performCommand("Сохранить", 12345L);

        Mockito.when(nonCommandHandler.getPassword(Mockito.eq("password"), Mockito.eq(12345L), Mockito.eq(State.SAVE_STEP_2)))
                .thenReturn(new Response("Введите описание пароля", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.SAVE_STEP_1);
        Response secondStep = commandService.performCommand("password", 12345L);

        Mockito.when(nonCommandHandler.getDescription(Mockito.eq("description"), Mockito.eq(12345L), Mockito.eq(State.NONE), Mockito.isNull()))
                .thenReturn(new Response("Пароль успешно сохранён", new Keyboard(List.of())));
        Mockito.when(nonCommandHandler.getDescription(Mockito.eq("description"), Mockito.eq(12345L), Mockito.eq(State.SAVE_STEP_3), Mockito.isNull()))
                .thenReturn(new Response("Установить напоминание о смене пароля? Стандартное значение 30 дней, сохранить?", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.SAVE_STEP_2);
        Response thirdStep = commandService.performCommand("description", 12345L);

        Mockito.when(nonCommandHandler.getAgreement(Mockito.eq("Нет"), Mockito.eq(12345L)))
                .thenReturn(new Response("Через сколько дней напомнить о смене пароля? (0 - не ставить напоминание)", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.SAVE_STEP_3);
        Response fourthStep = commandService.performCommand("Нет", 12345L);

        Mockito.when(nonCommandHandler.getRemindDays(Mockito.eq("3"), Mockito.eq(12345L), Mockito.eq(State.NONE)))
                .thenReturn(new Response("Пароль успешно сохранён", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.SAVE_STEP_4);
        Response fifthStep = commandService.performCommand("3", 12345L);

        Assertions.assertEquals("Введите пароль", firstStep.message());
        Assertions.assertEquals("Введите описание пароля", secondStep.message());
        Assertions.assertEquals("Установить напоминание о смене пароля? Стандартное значение 30 дней, сохранить?", thirdStep.message());
        Assertions.assertEquals("Через сколько дней напомнить о смене пароля? (0 - не ставить напоминание)", fourthStep.message());
        Assertions.assertEquals("Пароль успешно сохранён", fifthStep.message());
    }

    /**
     * Тест команды /edit с клавиатуры
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
     * Тест команды /del с клавиатуры
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
     * Тест команды /sort с клавиатуры
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
     * Тест команды /find с клавиатуры
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
     * Тест команды /remind с клавиатуры
     */
    @Test
    void testPerformCommandRemindKeyboard() {
        Mockito.when(remindHandler.handle(Mockito.eq(new String[]{"Напомнить"}), Mockito.eq(12345L)))
                .thenReturn(new Response("Введите индекс пароля", new Keyboard(List.of())));
        Response firstStep = commandService.performCommand("Напомнить", 12345L);

        Mockito.when(nonCommandHandler.getIndexPassword(Mockito.eq("1"), Mockito.eq(12345L)))
                .thenReturn(new Response("Через сколько дней напомнить о смене пароля?", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.REMIND_STEP_1);
        Response secondStep = commandService.performCommand("1", 12345L);

        Mockito.when(nonCommandHandler.getRemindDays(Mockito.eq("3"), Mockito.eq(12345L), Mockito.eq(State.NONE)))
                .thenReturn(new Response("Напоминание установлено", new Keyboard(List.of())));
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L))).thenReturn(State.REMIND_STEP_2);
        Response thirdStep = commandService.performCommand("3", 12345L);

        Assertions.assertEquals("Введите индекс пароля", firstStep.message());
        Assertions.assertEquals("Через сколько дней напомнить о смене пароля?", secondStep.message());
        Assertions.assertEquals("Напоминание установлено", thirdStep.message());
    }

}
