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
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.service.PasswordService;

import java.util.ArrayList;

import static ru.naumen.bot.Constants.ENTER_PASSWORD;
import static ru.naumen.bot.Constants.PASSWORD_SAVED_MESSAGE;
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

        Assertions.assertEquals(ENTER_PASSWORD, response.message());
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
        Assertions.assertEquals(PASSWORD_SAVED_MESSAGE, response.message());
        Assertions.assertEquals(NONE, response.botState());
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
        Assertions.assertEquals(PASSWORD_SAVED_MESSAGE, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }
}
