package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.ArrayList;

import static ru.naumen.bot.Constants.ENTER_PASSWORD;
import static ru.naumen.model.State.NONE;
import static ru.naumen.model.State.SAVE_STEP_1;

/**
 * Класс модульных тестов для SaveHandler
 */
class SaveHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private RemindScheduler remindScheduler;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private SaveHandler saveHandler;


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
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = saveHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите пароль", response.message());
        Assertions.assertEquals(SAVE_STEP_1, response.botState());
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
        Assertions.assertEquals(NONE, response.botState());
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
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест сохранения пароля с напоминанием
     */
    @Test
    void testSavePassword_WithRemind() throws UserNotFoundException {
        String[] command = {"/save", "pass", "desc", "1"};
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(validationService.isValidDays(1)).thenReturn(true);
        Mockito.when(passwordService.createUserPassword("pass", "desc", 12345L)).thenReturn("uuid");

        Response response = saveHandler.handle(command, 12345L);

        Mockito.verify(passwordService).createUserPassword("pass", "desc", 12345L);
        Mockito.verify(remindScheduler).scheduleRemind("Напоминание: обновите пароль для desc", 12345L, "uuid", 86_400_000L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }
}
