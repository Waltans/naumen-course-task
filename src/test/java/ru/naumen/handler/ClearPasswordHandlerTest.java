package ru.naumen.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.DecryptException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для хэндлера по очистке паролей
 */
@ExtendWith(MockitoExtension.class)
class ClearPasswordHandlerTest {

    private ClearPasswordHandler clearPasswordHandler;

    @Mock
    private UserStateCache userStateCache;
    @Mock
    private UserService userService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private EncodeService encodeService;
    @Mock
    private User user;

    private final long userId = 12345L;

    @BeforeEach
    void setUp() {
        clearPasswordHandler = new ClearPasswordHandler(userStateCache, userService, passwordService, encodeService);
    }

    /**
     * Тест, что корректно обрабатывается, если приходит команда без параметров
     */
    @Test
    void handle() {
        String[] splitCommand = {"/clear"};

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        assertEquals("Введите кодовое слово", response.message());
        assertEquals(State.CLEAR_1, response.botState());
        verify(userStateCache).setState(userId, State.CLEAR_1);
    }

    /**
     * Тест, что команда исполняется корректно и удаляет пароли, если кодовое слово правильное и не выбрасывается ошибка
     *
     * @throws Exception - обрабатываемые ошибки
     */
    @Test
    void handleAllCommand() throws Exception {
        String[] splitCommand = {"/clear", "code", "de"};

        when(userService.isExistCodeWord(userId)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(user);
        when(encodeService.decryptData(user.getCodePhrase())).thenReturn("code");

        doNothing().when(passwordService).deletePasswordByStartWord(userId, "de");

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        assertEquals(State.NONE, response.botState());
        assertEquals("Пароли, что начинаются с de удалены", response.message());
        verify(userStateCache).clearParamsForUser(userId);
        verify(userStateCache).setState(userId, State.NONE);
        verify(passwordService).deletePasswordByStartWord(userId, "de");
    }

    /**
     * Тест, что команда исполняется корректно, если введено неверное кодовое слово
     *
     * @throws Exception - обрабатываемые ошибки
     */
    @Test
    void handle_codeIncorrect() throws Exception {
        String[] splitCommand = {"/clear", "code", "de"};

        when(userService.isExistCodeWord(userId)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(user);
        when(encodeService.decryptData(user.getCodePhrase())).thenReturn("correctCode");

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        assertEquals(State.NONE, response.botState());
        assertEquals("Невозможно запустить операцию", response.message());
        verify(userStateCache).clearParamsForUser(userId);
        verify(userStateCache).setState(userId, State.NONE);
    }

    /**
     * Тест, что пользователю устанавливается статус и очищаются параметры,
     * если пользователь не существует. Отправляется корректное сообщение
     *
     * @throws UserNotFoundException - ошибка, пользователь не найден
     */
    @Test
    void handle_whenUserNotFound() throws Exception {
        String[] splitCommand = {"/clear", "code", "de"};

        when(userService.isExistCodeWord(userId)).thenThrow(new UserNotFoundException("User not found"));

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        assertEquals(State.NONE, response.botState());
        assertEquals("Пользователь не найден", response.message());
        verify(userStateCache).clearParamsForUser(userId);
    }

    /**
     * Тест, что команда работает корректно, если происходит ошибка дешифрования.
     * Очищаются параметры и статус и приходит корректное сообщение
     *
     * @throws DecryptException - ошибка дешифрования пароля
     */
    @Test
    void handle_decryptException() throws Exception {
        String[] splitCommand = {"/clear", "code", "de"};

        when(userService.isExistCodeWord(userId)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(user);
        when(encodeService.decryptData(user.getCodePhrase())).thenThrow(new DecryptException("Decrypt error", new RuntimeException()));

        Response response = clearPasswordHandler.handle(splitCommand, userId);

        assertEquals(State.NONE, response.botState());
        assertEquals("Ошибка при дешифровании кодового слова", response.message());
        verify(userStateCache).clearParamsForUser(userId);
        verify(userStateCache).setState(userId, State.NONE);
    }
}
