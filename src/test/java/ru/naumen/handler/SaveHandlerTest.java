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
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.service.PasswordService;

import java.util.ArrayList;
import java.util.List;

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
    private KeyboardCreator keyboardCreator;

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
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());

        Response response = saveHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите пароль", response.message());
    }

    /**
     * Тест сохранения пароля без описания
     */
    @Test
    void testSavePassword_NoDescription() throws UserNotFoundException {
        String[] command = {"/save", "password"};
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());

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
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());

        Response response = saveHandler.handle(command, 12345L);

        Mockito.verify(passwordService).createUserPassword("pass", "desc", 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест сохранения пароля с напоминанием
     */
    @Test
    void testSavePassword_WithRemind() throws UserNotFoundException {
        String[] command = {"/save", "pass", "desc", "3"};
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(passwordService.createUserPassword(Mockito.eq("pass"), Mockito.eq("desc"), Mockito.eq(12345L))).thenReturn("uuid");

        Response response = saveHandler.handle(command, 12345L);

        Response remindResponse = new Response("Напоминание: обновите пароль для desc",
                keyboardCreator.createMainKeyboard());

        Mockito.verify(passwordService).createUserPassword(Mockito.eq("pass"), Mockito.eq("desc"), Mockito.eq(12345L));
        Mockito.verify(remindScheduler)
                .scheduleRemind(12345L,
                        "uuid",
                        259_200_000L, remindResponse);
        Assertions.assertEquals("Пароль успешно сохранён", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }


    /**
     * Тест невалидной команды
     */
    @Test
    void testSavePassword_InvalidCommand() {
        String[] command = {"/save", "1", "3", "1", "1"};

        Response response = saveHandler.handle(command, 12345L);

        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }
}
