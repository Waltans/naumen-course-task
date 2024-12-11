package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.command.Command;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.Keyboard;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.List;
import java.util.Map;

import static ru.naumen.model.State.*;

/**
 * Класс модульных тестов для NonCommandHandler
 */
class NonCommandHandlerTest {

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private EditHandler editHandler;

    @Mock
    private DeleteHandler deleteHandler;

    @Mock
    private SaveHandler saveHandler;

    @Mock
    private SortHandler sortHandler;

    @Mock
    private FindHandler findHandler;

    @Mock
    private PasswordService passwordService;
    @Mock
    private KeyboardCreator keyboardCreator;
    @Mock
    private AddCodePhraseHandler addCodePhraseHandler;
    @Mock
    private ClearPasswordHandler clearPasswordHandler;
    @Mock
    private RemindHandler remindHandler;

    private NonCommandHandler nonCommandHandler;

    /**
     * Перед каждым тестом создаёт объекты (не моки!)
     * класса поиска команд, отображения хэндлеров и тестируемого класса
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(State.NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());

        Map<String, CommandHandler> commandHandlers = Map.ofEntries(
                Map.entry("/edit", editHandler),
                Map.entry("/del", deleteHandler),
                Map.entry("/save", saveHandler),
                Map.entry("/sort", sortHandler),
                Map.entry("/find", findHandler),
                Map.entry("/remind", remindHandler),
                Map.entry("/code", addCodePhraseHandler),
                Map.entry("/clear", clearPasswordHandler)
        );

        nonCommandHandler = new NonCommandHandler(
                userStateCache,
                passwordService,
                commandHandlers,
                keyboardCreator
        );
    }

    /**
     * Тест метода получения сложности пароля
     */
    @Test
    void testGetComplexity() {
        Response response = nonCommandHandler
                .getComplexity("3", 12345L, State.SAVE_STEP_2, "complexity entered");


        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("3"));

        Mockito.verify(userStateCache).addParam(12345L, "3");
        Mockito.verify(userStateCache).addParam(12345L, "3");
        Assertions.assertEquals("complexity entered", response.message());
    }

    /**
     * Тест метода получения длины пароля
     */
    @Test
    void testGetPasswordLength() {
        Response response = nonCommandHandler.getPasswordLength("8", 12345L, State.SAVE_STEP_1);

        Assertions.assertEquals("Выберите сложность пароля", response.message());
        Mockito.verify(userStateCache).addParam(12345L, "8");
    }

    /**
     * Тест метода получения описания пароля при сохранении
     */
    @Test
    void testGetDescriptionWhenSave() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SAVE_STEP_2);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("pass"));
        Mockito.when(saveHandler.handle(Mockito.any(), Mockito.eq(12345L)))
                .thenReturn(new Response("pass saved", new Keyboard(List.of())));

        Response response = nonCommandHandler.getDescription(
                "desc", 12345L, State.NONE, null);

        Assertions.assertEquals("Установить напоминание о смене пароля? Стандартное значение 30 дней, сохранить?", response.message());
    }

    /**
     * Тест метода получения описания пароля при изменении
     */
    @Test
    void testGetDescriptionWhenEdit() {
        String[] splitCommand = {"/edit", "1", "12", "3", "desc"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.EDIT_STEP_4);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("1", "12", "3"));
        Mockito.when(editHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("pass updated", new Keyboard(List.of())));

        Response response = nonCommandHandler.getDescription("desc", 12345L, State.NONE, null);

        Assertions.assertEquals("pass updated", response.message());
    }

    /**
     * Тест метода получения пароля
     */
    @Test
    void testGetPassword() {
        Response response = nonCommandHandler.getPassword("pass", 12345L, State.SAVE_STEP_2);

        Assertions.assertEquals("Введите описание пароля", response.message());
        Mockito.verify(userStateCache).addParam(12345L, "pass");
    }

    /**
     * Тест метода получения индекса пароля при изменении
     */
    @Test
    void testGetIndexPasswordWhenEdit() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.EDIT_STEP_1);
        Mockito.when(passwordService.isValidPasswordIndex(1, 12345L)).thenReturn(true);
        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("Введите длину пароля", response.message());
    }

    /**
     * Тест метода получения индекса пароля при установке напоминания
     */
    @Test
    void testGetIndexPasswordWhenRemind() {
        Mockito.when(userStateCache.getUserState(Mockito.eq(12345L)))
                .thenReturn(REMIND_STEP_1);
        Mockito.when(passwordService.isValidPasswordIndex(Mockito.eq(1), Mockito.eq(12345L)))
                .thenReturn(true);

        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("Через сколько дней напомнить о смене пароля?", response.message());
    }

    /**
     * Тест метода получения индекса пароля при удалении
     */
    @Test
    void testGetIndexPasswordWhenDelete() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.DELETE_STEP_1);

        String[] splitCommand = {"/del", "1"};
        Mockito.when(deleteHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("pass deleted", new Keyboard(List.of())));
        Mockito.when(passwordService.isValidPasswordIndex(1, 12345L)).thenReturn(true);
        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("pass deleted", response.message());
    }

    /**
     * Тест метода получения типа сортировки
     */
    @Test
    void testGetSortType() {
        String[] splitCommand = {"Дате"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SORT_STEP_1);
        Mockito.when(sortHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("sorted", new Keyboard(List.of())));

        Response response = nonCommandHandler.getSortType("Дате", 12345L);

        Assertions.assertEquals("sorted", response.message());
    }

    /**
     * Тест метода получения поискового запроса
     */
    @Test
    void testGetSearchRequest() {
        String[] splitCommand = {"/find", "query"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.FIND_STEP_1);
        Mockito.when(findHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("found", new Keyboard(List.of())));

        Response response = nonCommandHandler.getSearchRequest("query", 12345L);

        Assertions.assertEquals("found", response.message());
    }

    /**
     * Тест метода получения дней до напоминания при установке напоминания
     */
    @Test
    void testGetRemindDaysWhenRemind() {
        long userId = 12345L;

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(REMIND_STEP_1);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(List.of("1"));

        String[] splitCommand = {"/remind", "1", "5"};
        Mockito.when(remindHandler.handle(Mockito.eq(splitCommand), Mockito.eq(userId)))
                .thenReturn(new Response("reminder set", new Keyboard(List.of())));

        Response response = nonCommandHandler.getRemindDays("5", userId, REMIND_STEP_2);

        Assertions.assertEquals("reminder set", response.message());
        Mockito.verify(userStateCache).addParam(userId, "5");
        Mockito.verify(userStateCache).setState(userId, REMIND_STEP_2);
    }


    /**
     * Тест метода получения дней до напоминания при сохранении
     */
    @Test
    void testGetRemindDaysWhenSave() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SAVE_STEP_4);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("111", "desc"));

        String[] splitCommand = {"/save", "111", "desc", "10"};
        Mockito.when(saveHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("password saved", new Keyboard(List.of())));

        Response response = nonCommandHandler.getRemindDays("10", 12345L, NONE);

        Assertions.assertEquals("password saved", response.message());
        Mockito.verify(userStateCache).setState(12345L, NONE);
    }

    /**
     * Тест, что метод запрашивает соглашение пользователя при верном количестве параметров
     * Команда должна возвращать верное состояние и ответ пользователя
     */
    @Test
    void getPhraseForClear() {
        long userId = 12345L;
        String phrase = "testPhrase";
        List<String> userParams = List.of("code");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CLEAR_2);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);

        Response actualResponse = nonCommandHandler.getPhraseForClear(phrase, userId);

        Assertions.assertEquals("Найдено 0 совпадений, вы точно хотите удалить все пароли, описание которых начинается на testPhrase?", actualResponse.message());
    }

    /**
     * Тест, что команда работает корректно, если пользователь соглашается на отчистку паролей
     */
    @Test
    void testClearSuccess() {
        long userId = 12345L;
        String phrase = "code";
        List<String> userParams = List.of(phrase, "qwe");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(State.CLEAR_3);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);
        Mockito.when(clearPasswordHandler.handle(
                        Mockito.eq(new String[]{Command.CLEAR.getCommand(), phrase, "qwe"}),
                        Mockito.eq(userId)))
                .thenReturn(new Response("Success", new Keyboard(List.of())));

        Response response = nonCommandHandler.getAgreement("да", userId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals("Success", response.message());
    }

    /**
     * Тест, что команда работает корректно, если пользователь отказывается от отчистки паролей
     */
    @Test
    void testClearReject() {
        long userId = 12345L;
        List<String> userParams = List.of("code");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CLEAR_3);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);

        Response response = nonCommandHandler.getAgreement("нет", userId);
        Assertions.assertEquals("Пароли не будут очищены", response.message());
    }

    /**
     * Тест, что при неверном состоянии будет соответствующий ответ
     */
    @Test
    void testGetPhraseForClear_failure() {
        long userId = 12345L;
        String phrase = "somethingWrong";

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(SAVE_STEP_1);

        Response actualResponse = nonCommandHandler.getPhraseForClear(phrase, userId);

        Assertions.assertEquals("Что-то пошло не так :( ", actualResponse.message());
        Mockito.verify(userStateCache).clearParamsForUser(userId);
    }

    /**
     * Тест, что метод выполняется корректно при состоянии CODE_PHRASE_1
     */
    @Test
    void testGetCodeWord() {
        long userId = 12345L;
        String codeWord = "newCode";

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CODE_PHRASE_1);
        Mockito.when(addCodePhraseHandler.handle(
                        new String[]{Command.ADD_CODE.getCommand(), codeWord}, userId))
                .thenReturn(new Response("Success", new Keyboard(List.of())));

        Response actualResponse = nonCommandHandler.getCodeWord(codeWord, userId);

        Assertions.assertEquals("Success", actualResponse.message());

        Mockito.verify(addCodePhraseHandler).handle(new String[]{Command.ADD_CODE.getCommand(), codeWord}, userId);
    }

    /**
     * Тест, что команда работает корректно при состоянии CLEAR_1
     */
    @Test
    void getCodeWord_clearState() {
        long userId = 12345L;
        String codeWord = "newCode";

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CLEAR_1);

        Response actualResponse = nonCommandHandler.getCodeWord(codeWord, userId);

        Assertions.assertEquals("Начало слова с которого вы хотите удалить пароли(ALL - если удалить все)",
                actualResponse.message());
        Mockito.verify(userStateCache).addParam(userId, codeWord);
        Mockito.verify(userStateCache).setState(userId, State.CLEAR_2);
    }

    /**
     * Тест, что команда работает корректно, если пришли у пользователя неподходящий статус
     */
    @Test
    void getCodeWord_IncorrectState() {
        long userId = 12345L;
        String codeWord = "somethingWrong";

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(SAVE_STEP_1);

        Response actualResponse = nonCommandHandler.getCodeWord(codeWord, userId);

        Assertions.assertEquals("Что-то пошло не так :( ", actualResponse.message());
        Mockito.verify(userStateCache).clearParamsForUser(userId);
    }

    /**
     * Тест, что сохранение работает корректно,
     * если пользователь соглашается с установкой стандартного числа дней до напоминания
     */
    @Test
    void testSaveAcceptRemind() {
        long userId = 12345L;
        List<String> userParams = List.of("pass", "desc");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(SAVE_STEP_3);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);
        Mockito.when(saveHandler.handle(new String[]{"/save", "pass", "desc", "30"}, userId))
                .thenReturn(new Response("Пароль успешно сохранён", new Keyboard(List.of())));

        Response response = nonCommandHandler.getAgreement("да", userId);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
    }

    /**
     * Тест, что сохранение работает корректно,
     * если пользователь отказывается от установки стандартного числа дней до напоминания
     */
    @Test
    void testSaveRejectRemind() {
        long userId = 12345L;
        List<String> userParams = List.of("pass", "desc");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(SAVE_STEP_3);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);

        Response response = nonCommandHandler.getAgreement("нет", userId);
        Assertions.assertEquals("Через сколько дней напомнить о смене пароля? (0 - не ставить напоминание)", response.message());
    }
}
