package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.ValidationService;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.ENTER_PASSWORD_COMPLEXITY;
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
                startHelpHandler
        );

        nonCommandHandler = new NonCommandHandler(
                userStateCache,
                validationService,
                handlerMapper
        );
    }

    /**
     * Тест метода получения сложности пароля
     */
    @Test
    void testGetComplexity() {
        Mockito.when(validationService.isValidComplexity("3")).thenReturn(true);
        Response response = nonCommandHandler.getComplexity("3", 12345L, SAVE_STEP_2, "complexity entered");

        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("3"));

        Assertions.assertEquals(SAVE_STEP_2, response.botState());
        Mockito.verify(userStateCache).addParam(12345L,"3");
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
        String[] splitCommand = {"/save", "pass", "desc"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SAVE_STEP_2);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("pass"));
        Mockito.when(saveHandler.handle(splitCommand, 12345L))
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
}
