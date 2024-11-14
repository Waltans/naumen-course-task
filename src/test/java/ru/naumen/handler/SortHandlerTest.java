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
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

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
        Mockito.when(userStateCache.getTotalUserParams()).thenReturn(new ConcurrentHashMap<>());
        Mockito.when(userStateCache.getTotalUserState()).thenReturn(new ConcurrentHashMap<>());
        userStateCache.getTotalUserParams().put(12345L, new ArrayList<>());
    }

    /**
     * Тест сортировки по описанию
     */
    @Test
    void testSortPasswords_ByDescription() {
        String[] command = {"Описанию"};
        List<UserPassword> userPasswords = List.of(
                new UserPassword("bdesc", "pass1", null),
                new UserPassword("adesc", "pass2", null)
        );

        String expectedResponse = "\n" +
                "1) Сайт: adesc, Пароль: dpass2\n" +
                "2) Сайт: bdesc, Пароль: dpass1";

        userStateCache.getTotalUserState().put(12345L, SORT_STEP_1);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dpass1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dpass2");

        Response response = sortHandler.sortPasswords(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест сортировки по дате
     */
    @Test
    void testSortPasswords_ByDate() {
        String[] command = {"Дате"};
        List<UserPassword> userPasswords = List.of(
                new UserPassword("uuid1", "desc1", "pass1", null, LocalDate.of(2010, 1 ,1)),
                new UserPassword("uuid2", "desc2", "pass2", null, LocalDate.of(2013, 1,1)),
                new UserPassword("uuid3", "desc3", "pass3", null, LocalDate.of(2012, 1,1))
        );

        String expectedResponse = "\n" +
                "1) Сайт: desc1, Пароль: dpass1\n" +
                "2) Сайт: desc3, Пароль: dpass3\n" +
                "3) Сайт: desc2, Пароль: dpass2";

        userStateCache.getTotalUserState().put(12345L, SORT_STEP_1);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dpass1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dpass2");
        Mockito.when(encodeService.decryptData("pass3")).thenReturn("dpass3");

        Response response = sortHandler.sortPasswords(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест сортировки, если нет паролей
     */
    @Test
    void testSortPasswords_Empty() {
        String[] command = {"Дате"};
        List<UserPassword> userPasswords = new ArrayList<>();

        userStateCache.getTotalUserState().put(12345L, SORT_STEP_1);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        Response response = sortHandler.sortPasswords(command, 12345L);

        Assertions.assertEquals("Нет ни одного пароля. Справка: /help", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест вызова сортировки
     */
    @Test
    void testSortPasswords_WithoutParams() {
        String[] command = {"Сортировать"};

        userStateCache.getTotalUserState().put(12345L, NONE);
        Response response = sortHandler.sortPasswords(command, 12345L);

        Assertions.assertEquals(CHOOSE_SORT_TYPE, response.message());
        Assertions.assertEquals(SORT_STEP_1, response.botState());
    }
}
