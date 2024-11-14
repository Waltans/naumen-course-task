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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

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
        List<UserPassword> userPasswords = List.of(new UserPassword("desc", "pass", null));

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass")).thenReturn("dpass");

        Response response = findHandler.findPasswords(command, 12345L);

        Assertions.assertEquals("\n1) Сайт: desc, Пароль: dpass", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест поиска паролей, если не найдены
     */
    @Test
    void testFindPasswords_WithNoResults() {
        String[] command = {"/find", "no"};
        List<UserPassword> userPasswords = List.of(new UserPassword("desc", "pass", null));

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        Response response = findHandler.findPasswords(command, 12345L);

        Assertions.assertEquals(NO_PASSWORDS_FOUND, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест поиска паролей при вводе команды с кнопки
     */
    @Test
    void testFindPasswords_WithoutParams() {
        String[] command = {"Искать"};
        Mockito.when(userStateCache.getTotalUserState()).thenReturn(new ConcurrentHashMap<>());
        Mockito.when(userStateCache.getTotalUserParams()).thenReturn(new ConcurrentHashMap<>());

        Response response = findHandler.findPasswords(command, 12345L);

        Assertions.assertEquals(ENTER_SEARCH_REQUEST, response.message());
        Assertions.assertEquals(FIND_STEP_1, response.botState());
    }
}
