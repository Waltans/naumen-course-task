package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.model.State.*;

/**
 * Класс модульных тестов для NonCommandHandler
 */
class NonCommandHandlerTest {

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private ValidationService validationService;

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
    private GenerateHandler generateHandler;

    @Mock
    private ListHandler listHandler;

    @Mock
    private StartHelpHandler startHelpHandler;
    @Mock
    private AddCodePhraseHandler addCodePhraseHandler;
    @Mock
    private ClearPasswordHandler clearPasswordHandler;
    @Mock
    private RemindHandler remindHandler;
    @Mock
    private PasswordService passwordService;

    private NonCommandHandler nonCommandHandler;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        HandlerMapper handlerMapper = new HandlerMapper(
                deleteHandler,
                editHandler,
                findHandler,
                generateHandler,
                listHandler,
                saveHandler,
                sortHandler,
                startHelpHandler,
                remindHandler,
                addCodePhraseHandler,
                clearPasswordHandler
        );

        nonCommandHandler = new NonCommandHandler(
                userStateCache,
                validationService,
                handlerMapper,
                passwordService
        );
    }

    /**
     * Тест метода получения сложности пароля
     */
    @Test
    void testGetComplexity() {
        Mockito.when(validationService.isValidComplexity("3")).thenReturn(true);

        Response response = nonCommandHandler.getComplexity("3", 12345L, SAVE_STEP_3, "complexity entered");

        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("3"));

        Assertions.assertEquals(SAVE_STEP_3, response.botState());
        Mockito.verify(userStateCache).addParam(12345L, "3");
        Assertions.assertEquals("complexity entered", response.message());
    }

    /**
     * Тест метода получения длины пароля
     */
    @Test
    void testGetPasswordLength() {
        Mockito.when(validationService.isValidLength(8)).thenReturn(true);

        Response response = nonCommandHandler.getPasswordLength("8", 12345L, SAVE_STEP_1);

        Assertions.assertEquals("Выберите сложность пароля", response.message());
        Assertions.assertEquals(SAVE_STEP_1, response.botState());
        Mockito.verify(userStateCache).addParam(12345L, "8");
    }

    /**
     * Тест метода получения описания пароля при сохранении
     */
    @Test
    void testGetDescriptionWhenSave() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SAVE_STEP_2);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("pass"));

        Response response = nonCommandHandler.getDescription("desc", 12345L, NONE, null);

        Assertions.assertEquals("Установить напоминание о смене пароля? Стандартное значение 30 дней, сохранить?", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения описания пароля при изменении
     */
    @Test
    void testGetDescriptionWhenEdit() {
        String[] splitCommand = {"/edit", "1", "12", "3", "desc"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(EDIT_STEP_4);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("1", "12", "3"));
        Mockito.when(editHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("pass updated", NONE));

        Response response = nonCommandHandler.getDescription("desc", 12345L, NONE, null);

        Assertions.assertEquals("pass updated", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения пароля
     */
    @Test
    void testGetPassword() {
        Response response = nonCommandHandler.getPassword("pass", 12345L, SAVE_STEP_2);

        Assertions.assertEquals("Введите описание пароля", response.message());
        Assertions.assertEquals(SAVE_STEP_2, response.botState());
        Mockito.verify(userStateCache).addParam(12345L, "pass");
    }

    /**
     * Тест метода получения индекса пароля при изменении
     */
    @Test
    void testGetIndexPasswordWhenEdit() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(EDIT_STEP_1);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);

        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("Введите длину пароля", response.message());
        Assertions.assertEquals(EDIT_STEP_2, response.botState());
    }

    /**
     * Тест метода получения индекса пароля при установке напоминания
     */
    @Test
    void testGetIndexPasswordWhenRemind() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(REMIND_STEP_1);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);

        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("Через сколько дней напомнить о смене пароля?", response.message());
        Assertions.assertEquals(REMIND_STEP_2, response.botState());
    }

    /**
     * Тест метода получения индекса пароля при удалении
     */
    @Test
    void testGetIndexPasswordWhenDelete() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(DELETE_STEP_1);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);

        String[] splitCommand = {"/del", "1"};
        Mockito.when(deleteHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("pass deleted", NONE));

        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("pass deleted", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения типа сортировки
     */
    @Test
    void testGetSortType() {
        String[] splitCommand = {"Дате"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SORT_STEP_1);
        Mockito.when(sortHandler.handle(splitCommand, 12345L)).thenReturn(new Response("sorted", NONE));

        Response response = nonCommandHandler.getSortType("Дате", 12345L);

        Assertions.assertEquals("sorted", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения поискового запроса
     */
    @Test
    void testGetSearchRequest() {
        String[] splitCommand = {"/find", "query"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(FIND_STEP_1);
        Mockito.when(findHandler.handle(splitCommand, 12345L)).thenReturn(new Response("found", NONE));

        Response response = nonCommandHandler.getSearchRequest("query", 12345L);

        Assertions.assertEquals("found", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения дней до напоминания при установке напоминания
     */
    @Test
    void testGetRemindDaysWhenRemind() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(REMIND_STEP_1);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("1"));
        Mockito.when(validationService.isValidDays(5)).thenReturn(true);

        String[] splitCommand = {"/remind", "1", "5"};
        Mockito.when(remindHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("reminder set", NONE));

        Response response = nonCommandHandler.getRemindDays("5", 12345L, REMIND_STEP_2);

        Assertions.assertEquals("reminder set", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).addParam(12345L, "5");
        Mockito.verify(userStateCache).setState(12345L, REMIND_STEP_2);
    }

    /**
     * Тест метода получения дней до напоминания при сохранении
     */
    @Test
    void testGetRemindDaysWhenSave() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SAVE_STEP_4);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("111", "desc"));
        Mockito.when(validationService.isValidDays(10)).thenReturn(true);

        String[] splitCommand = {"/save", "111", "desc", "10"};
        Mockito.when(saveHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("password saved", NONE));

        Response response = nonCommandHandler.getRemindDays("10", 12345L, NONE);

        Assertions.assertEquals("password saved", response.message());
        Assertions.assertEquals(NONE, response.botState());
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
        List<String> userParams = List.of("/clear code");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CLEAR_2);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);

        Response actualResponse = nonCommandHandler.getPhraseForClear(phrase, userId);

        Assertions.assertEquals(CLEAR_3, actualResponse.botState());
        Assertions.assertEquals("Найдено 0 совпадений, вы точно хотите удалить все пароли, описание которых начинается на testPhrase", actualResponse.message());
    }

    /**
     * Тест, что команда работает корректно, если пользователь соглашается на отчистку паролей
     */
    @Test
    void clear_success() {
        long userId = 12345L;
        String phrase = "code";
        List<String> userParams = List.of("/clear", "code");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CLEAR_3);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);
        Mockito.when(clearPasswordHandler.handle(new String[]{Command.CLEAR, userParams.get(0), phrase}, userId))
                .thenReturn(new Response("Success", NONE));

        Response response = nonCommandHandler.getAgreement("да", userId);
        Assertions.assertEquals("Success", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест, что команда работает корректно, если пользователь отказывается от отчистки паролей
     */
    @Test
    void clear_failure() {
        long userId = 12345L;
        List<String> userParams = List.of("/clear", "code");

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CLEAR_3);
        Mockito.when(userStateCache.getUserParams(userId)).thenReturn(userParams);

        Response response = nonCommandHandler.getAgreement("нет", userId);
        Assertions.assertEquals("Пароли не будут очищены", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест, что при неверном состоянии будет правильный ответ
     */
    @Test
    void getPhraseForClear_failure() {
        long userId = 12345L;
        String phrase = "somethingWrong";

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(SAVE_STEP_1);

        Response actualResponse = nonCommandHandler.getPhraseForClear(phrase, userId);

        Assertions.assertEquals("Что-то пошло не так :( ", actualResponse.message());
        Assertions.assertEquals(SAVE_STEP_1, actualResponse.botState());
        Mockito.verify(userStateCache).clearParamsForUser(userId);
    }

    /**
     * Тест, что метод выполняется корректно при состоянии CODE_PHRASE_1
     */
    @Test
    void getCodeWord() {
        long userId = 12345L;
        String codeWord = "newCode";

        Mockito.when(userStateCache.getUserState(userId)).thenReturn(CODE_PHRASE_1);
        Mockito.when(addCodePhraseHandler.handle(
                        new String[]{Command.ADD_CODE, codeWord}, userId))
                .thenReturn(new Response("Success", NONE));

        Response actualResponse = nonCommandHandler.getCodeWord(codeWord, userId);

        Assertions.assertEquals("Success", actualResponse.message());
        Assertions.assertEquals(NONE, actualResponse.botState());
        Mockito.verify(addCodePhraseHandler).handle(new String[]{Command.ADD_CODE, codeWord}, userId);
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

        Assertions.assertEquals(CLEAR_2, actualResponse.botState());
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
        Assertions.assertEquals(SAVE_STEP_1, actualResponse.botState());
        Mockito.verify(userStateCache).clearParamsForUser(userId);
    }
}
