package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.ArrayList;

/**
 * Класс модульных тестов для SaveHandler
 */
class SaveHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @InjectMocks
    private SaveHandler saveHandler;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест сохранения пароля с кнопки
     */
    @Test
    void testSavePassword_WithoutParams() {
        String[] command = {"Сохранить"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(State.NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = saveHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите пароль", response.message());
    }

    /**
     * Тест сохранения пароля без описания
     */
    @Test
    void testSavePassword_NoDescription() throws UserNotFoundException {
        String[] command = {"/save", "password"};
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = saveHandler.handle(command, 12345L);

        Mockito.verify(passwordService).createUserPassword("password", "Неизвестно", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест сохранения пароля с описанием
     */
    @Test
    void testSavePassword_WithDescription() throws UserNotFoundException {
        String[] command = {"/save", "pass", "desc"};
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = saveHandler.handle(command, 12345L);

        Mockito.verify(passwordService).createUserPassword("pass", "desc", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }
}
