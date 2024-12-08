package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.keyboards.KeyboardCreator;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

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

    @Mock
    private KeyboardCreator keyboardCreator;

    /**
     * Инициализирует моки перед каждым тестом
     */
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
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест поиска паролей, если не найдены
     */
    @Test
    void testFindPasswords_WithNoResults() {
        String[] command = {"/find", "no"};
        Mockito.when(passwordService.getUserPasswordsWithPartialDescription(12345L, "no")).thenReturn(List.of());

        Response response = findHandler.handle(command, 12345L);

        Assertions.assertEquals("Не найдены пароли по вашему запросу", response.message());
    }

    /**
     * Тест поиска паролей при вводе команды с кнопки
     */
    @Test
    void testFindPasswords_WithoutParams() {
        String[] command = {"Искать"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(State.NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());

        Response response = findHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите поисковый запрос", response.message());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testFindPasswords_InvalidCommand() {
        String[] command = {"/find", "1", "3", "1"};

        Response response = findHandler.handle(command, 12345L);

        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }
}
