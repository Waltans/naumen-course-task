package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.UserService;

/**
 * Тестовый класс для хэндлера по очистке паролей
 */
@ExtendWith(MockitoExtension.class)
class ClearPasswordHandlerTest {
    @Mock
    private UserStateCache userStateCache;
    @Mock
    private UserService userService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private EncodeService encodeService;
    @Mock
    private KeyboardCreator keyboardCreator;
    @InjectMocks
    private ClearPasswordHandler clearPasswordHandler;

    /**
     * Тест, что корректно обрабатывается, если приходит команда без параметров
     */
    @Test
    void handleNoParams() {
        long userId = 1L;
        String[] splitCommand = {"/clear"};

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        Assertions.assertEquals("Введите кодовое слово", response.message());
        Mockito.verify(userStateCache).setState(userId, State.CLEAR_1);
    }

    /**
     * Тест, что команда исполняется корректно и удаляет пароли, если кодовое слово правильное
     */
    @Test
    void handleAllCommand() throws UserNotFoundException {
        long userId = 1L;
        User user = new User(userId);
        String[] splitCommand = {"/clear", "code", "de"};

        Mockito.when(userService.isExistCodeWord(userId)).thenReturn(true);
        Mockito.when(userService.getUserById(userId)).thenReturn(user);
        Mockito.when(encodeService.decryptData(user.getCodePhrase())).thenReturn("code");

        Mockito.when(passwordService.deletePasswordByStartWord(userId, "de")).thenReturn(1);

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        Assertions.assertEquals("Удален 1 пароль", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(userId);
        Mockito.verify(userStateCache).setState(userId, State.NONE);
        Mockito.verify(passwordService).deletePasswordByStartWord(userId, "de");
    }

    /**
     * Тест, если введено неверное кодовое слово
     */
    @Test
    void handleCodeIncorrect() throws UserNotFoundException {
        long userId = 1L;
        User user = new User(userId);
        String[] splitCommand = {"/clear", "code", "de"};

        Mockito.when(userService.isExistCodeWord(userId)).thenReturn(true);
        Mockito.when(userService.getUserById(userId)).thenReturn(user);
        Mockito.when(encodeService.decryptData(user.getCodePhrase())).thenReturn("correctCode");

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        Assertions.assertEquals("Невозможно запустить операцию", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(userId);
        Mockito.verify(userStateCache).setState(userId, State.NONE);
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testClear_InvalidCommand() {
        String[] command = {"/clear", "1", "2", "3"};

        Response response = clearPasswordHandler.handle(command, 12345L);

        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }
}
