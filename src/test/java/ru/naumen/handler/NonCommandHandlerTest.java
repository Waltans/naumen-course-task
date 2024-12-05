package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.List;
import java.util.Map;

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

        Map<String, CommandHandler> commandHandlers = Map.of(
                "/edit", editHandler,
                "/del", deleteHandler,
                "/save", saveHandler,
                "/sort", sortHandler,
                "/find", findHandler
        );

        nonCommandHandler = new NonCommandHandler(
                userStateCache,
                passwordService,
                commandHandlers
        );
    }

    /**
     * Тест метода получения сложности пароля
     */
    @Test
    void testGetComplexity() {
        Response response = nonCommandHandler.getComplexity("3", 12345L, State.SAVE_STEP_2, "complexity entered");

        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("3"));

        Mockito.verify(userStateCache).addParam(12345L,"3");
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
        String[] splitCommand = {"/save", "pass", "desc"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SAVE_STEP_2);
        Mockito.when(userStateCache.getUserParams(12345L)).thenReturn(List.of("pass"));
        Mockito.when(saveHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("pass saved"));

        Response response = nonCommandHandler.getDescription("desc", 12345L, State.NONE, null);

        Assertions.assertEquals("pass saved", response.message());
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
                .thenReturn(new Response("pass updated"));

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
     * Тест метода получения индекса пароля при удалении
     */
    @Test
    void testGetIndexPasswordWhenDelete() {
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.DELETE_STEP_1);

        String[] splitCommand = {"/del", "1"};
        Mockito.when(deleteHandler.handle(splitCommand, 12345L))
                .thenReturn(new Response("pass deleted"));
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
        Mockito.when(sortHandler.handle(splitCommand, 12345L)).thenReturn(new Response("sorted"));

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
        Mockito.when(findHandler.handle(splitCommand, 12345L)).thenReturn(new Response("found"));

        Response response = nonCommandHandler.getSearchRequest("query", 12345L);

        Assertions.assertEquals("found", response.message());
    }
}
