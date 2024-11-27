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
import ru.naumen.exception.IncorrectSortTypeException;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.SortType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ru.naumen.model.State.NONE;
import static ru.naumen.model.State.SORT_STEP_1;

/**
 * Класс модульных тестов для SortHandler
 */
class SortHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private EncodeService encodeService;

    @InjectMocks
    private SortHandler sortHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());
    }

    /**
     * Тест сортировки по описанию
     */
    @Test
    void testSortPasswords_ByDescription() throws IncorrectSortTypeException {
        String[] command = {"Описанию"};
        List<UserPassword> passwords = List.of(
                new UserPassword("adesc", "pass2", null),
                new UserPassword("bdesc", "pass1", null)
        );

        String expectedResponse = "\n" +
                "1) Сайт: adesc, Пароль: dpass2\n" +
                "2) Сайт: bdesc, Пароль: dpass1";

        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SORT_STEP_1);
        Mockito.when(passwordService.getUserPasswordsSorted(12345L, SortType.BY_DESCRIPTION)).thenReturn(passwords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dpass1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dpass2");

        Response response = sortHandler.handle(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест сортировки по дате
     */
    @Test
    void testSortPasswords_ByDate() throws IncorrectSortTypeException {
        String[] command = {"Дате"};
        List<UserPassword> passwords = List.of(
                new UserPassword("uuid1", "desc1", "pass1", null, LocalDate.of(2010, 1, 1)),
                new UserPassword("uuid3", "desc3", "pass3", null, LocalDate.of(2012, 1, 1)),
                new UserPassword("uuid2", "desc2", "pass2", null, LocalDate.of(2013, 1, 1))
        );

        String expectedResponse = "\n" +
                "1) Сайт: desc1, Пароль: dpass1\n" +
                "2) Сайт: desc3, Пароль: dpass3\n" +
                "3) Сайт: desc2, Пароль: dpass2";

        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SORT_STEP_1);
        Mockito.when(passwordService.getUserPasswordsSorted(12345L, SortType.BY_DATE)).thenReturn(passwords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dpass1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dpass2");
        Mockito.when(encodeService.decryptData("pass3")).thenReturn("dpass3");

        Response response = sortHandler.handle(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест сортировки, если нет паролей
     */
    @Test
    void testSortPasswords_Empty() throws IncorrectSortTypeException {
        String[] command = {"Дате"};
        List<UserPassword> passwords = new ArrayList<>();

        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(SORT_STEP_1);
        Mockito.when(passwordService.getUserPasswordsSorted(12345L, SortType.BY_DATE)).thenReturn(passwords);

        Response response = sortHandler.handle(command, 12345L);

        Assertions.assertEquals("Нет ни одного пароля. Справка: /help", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест вызова сортировки
     */
    @Test
    void testSortPasswords_WithoutParams() {
        String[] command = {"Сортировать"};

        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(NONE);
        Response response = sortHandler.handle(command, 12345L);

        Assertions.assertEquals("Отсортировать пароли по:", response.message());
        Assertions.assertEquals(SORT_STEP_1, response.botState());
    }
}
