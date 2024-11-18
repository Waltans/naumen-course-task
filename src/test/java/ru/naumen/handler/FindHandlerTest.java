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

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.ENTER_SEARCH_REQUEST;
import static ru.naumen.bot.Constants.NO_PASSWORDS_FOUND;
import static ru.naumen.model.State.FIND_STEP_1;
import static ru.naumen.model.State.NONE;

/**
 * Класс модульных тестов для FindHandler
 */
class FindHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private EncodeService encodeService;

    @InjectMocks
    private FindHandler findHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест поиска паролей, если найдены
     */
    @Test
    void testFindPasswords() {
        String[] command = {"/find", "de"};
        List<UserPassword> passwords = List.of(new UserPassword("desc", "pass", null));

        Mockito.when(passwordService.getUserPasswordsWithPartialDescription(12345L, "de")).thenReturn(passwords);
        Mockito.when(encodeService.decryptData("pass")).thenReturn("dpass");

        Response response = findHandler.handle(command, 12345L);

        Assertions.assertEquals("\n1) Сайт: desc, Пароль: dpass", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест поиска паролей, если не найдены
     */
    @Test
    void testFindPasswords_WithNoResults() {
        String[] command = {"/find", "no"};
        Mockito.when(passwordService.getUserPasswordsWithPartialDescription(12345L, "no")).thenReturn(new ArrayList<>());

        Response response = findHandler.handle(command, 12345L);

        Assertions.assertEquals(NO_PASSWORDS_FOUND, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест поиска паролей при вводе команды с кнопки
     */
    @Test
    void testFindPasswords_WithoutParams() {
        String[] command = {"Искать"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = findHandler.handle(command, 12345L);

        Assertions.assertEquals(ENTER_SEARCH_REQUEST, response.message());
        Assertions.assertEquals(FIND_STEP_1, response.botState());
    }
}