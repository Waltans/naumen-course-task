package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.bot.Constants.*;
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

    @InjectMocks
    private NonCommandHandler nonCommandHandler;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(userStateCache.getTotalUserParams()).thenReturn(new ConcurrentHashMap<>());
        Mockito.when(userStateCache.getTotalUserState()).thenReturn(new ConcurrentHashMap<>());
        userStateCache.getTotalUserParams().put(12345L, new ArrayList<>());
    }

    /**
     * Тест метода получения сложности пароля
     */
    @Test
    void testGetComplexity() {
        Mockito.doNothing().when(validationService).validateComplexity(3);

        Response response = nonCommandHandler.getComplexity("3", 12345L, SAVE_STEP_2, "complexity entered");

        List<String> params = userStateCache.getTotalUserParams().get(12345L);
        Assertions.assertEquals("3", params.get(0));
        Assertions.assertEquals(SAVE_STEP_2, response.botState());
        Assertions.assertEquals("complexity entered", response.message());
    }

    /**
     * Тест метода получения длины пароля
     */
    @Test
    void testGetPasswordLength() {
        Mockito.doNothing().when(validationService).validateLength(8);

        Response response = nonCommandHandler.getPasswordLength("8", 12345L, SAVE_STEP_1);

        Assertions.assertEquals(ENTER_PASSWORD_COMPLEXITY, response.message());
        Assertions.assertEquals(SAVE_STEP_1, response.botState());
        Assertions.assertEquals("8", userStateCache.getTotalUserParams().get(12345L).get(0));
    }

    /**
     * Тест метода получения описания пароля при сохранении
     */
    @Test
    void testGetDescriptionWhenSave() {
        userStateCache.getTotalUserState().put(12345L, SAVE_STEP_2);
        userStateCache.getTotalUserParams().get(12345L).add("pass");

        String[] splitCommand = {"/save", "pass", "desc"};
        Mockito.when(saveHandler.savePassword(splitCommand, 12345L))
                .thenReturn(new Response("pass saved", NONE));

        Response response = nonCommandHandler.getDescription("desc", 12345L, NONE, null);

        Assertions.assertEquals("pass saved", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения описания пароля при изменении
     */
    @Test
    void testGetDescriptionWhenEdit() {
        userStateCache.getTotalUserState().put(12345L, EDIT_STEP_4);
        userStateCache.getTotalUserParams().get(12345L).add("1");
        userStateCache.getTotalUserParams().get(12345L).add("12");
        userStateCache.getTotalUserParams().get(12345L).add("3");

        String[] splitCommand = {"/edit", "1", "12", "3", "desc"};
        Mockito.when(editHandler.updatePassword(splitCommand, 12345L))
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
        Assertions.assertEquals("pass", userStateCache.getTotalUserParams().get(12345L).get(0));
    }

    /**
     * Тест метода получения индекса пароля при изменении
     */
    @Test
    void testGetIndexPasswordWhenEdit() {
        userStateCache.getTotalUserState().put(12345L, EDIT_STEP_1);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);

        Response response = nonCommandHandler.getIndexPassword("1", 12345L);

        Assertions.assertEquals("Введите длину пароля", response.message());
        Assertions.assertEquals(EDIT_STEP_2, response.botState());
    }

    /**
     * Тест метода получения индекса пароля при удалении
     */
    @Test
    void testGetIndexPasswordWhenDelete() {
        userStateCache.getTotalUserState().put(12345L, DELETE_STEP_1);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);

        String[] splitCommand = {"/del", "1"};
        Mockito.when(deleteHandler.deletePassword(splitCommand, 12345L))
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
        userStateCache.getTotalUserState().put(12345L, SORT_STEP_1);
        String[] splitCommand = {"Дате"};
        Mockito.when(sortHandler.sortPasswords(splitCommand, 12345L)).thenReturn(new Response("sorted", NONE));

        Response response = nonCommandHandler.getSortType("Дате", 12345L);

        Assertions.assertEquals("sorted", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест метода получения поискового запроса
     */
    @Test
    void testGetSearchRequest() {
        userStateCache.getTotalUserState().put(12345L, FIND_STEP_1);
        String[] splitCommand = {"/find", "query"};
        Mockito.when(findHandler.findPasswords(splitCommand, 12345L)).thenReturn(new Response("found", NONE));

        Response response = nonCommandHandler.getSearchRequest("query", 12345L);

        Assertions.assertEquals("found", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }
}
