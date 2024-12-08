package ru.naumen.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserCodePhraseException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static ru.naumen.bot.constants.Schedules.MILLIS_IN_A_DAY;

/**
 * Тестовый класс для хэндлера по добавлению кодового слова
 */
@ExtendWith(MockitoExtension.class)
class AddCodePhraseHandlerTest {

    @Mock
    private UserStateCache userStateCache;
    @Mock
    private UserService userService;
    @Mock
    private RemindScheduler scheduler;
    @InjectMocks
    private AddCodePhraseHandler handler;
    @Mock
    private KeyboardCreator keyboardCreator;

    /**
     * Проверяем, что при получении команды без параметров
     * пользователю устанавливается статус и возвращается верный ответ
     */
    @Test
    void handle_noParams() {
        long userId = 1L;
        String[] splitCommand = {"/code"};

        Response response = handler.handle(splitCommand, userId);

        verify(userStateCache).setState(userId, State.CODE_PHRASE_1);
        assertEquals("Введите кодовое слово", response.message());
    }

    /**
     * Тест, что кодовое слово установилось успешно, установился таймер и
     * пользователю пришел верный ответ с правильным статусом
     *
     * @throws Exception
     */
    @Test
    void handle_addCodePhraseSuccessfully() throws Exception {
        long userId = 1L;
        String[] splitCommand = {"/code", "phrase"};

        Response response = handler.handle(splitCommand, userId);

        verify(userService).addCodeWordForUser(userId, "phrase");
        verify(scheduler).scheduleRemind(
                eq("Вам необходимо заменить кодовое слово"),
                eq(userId),
                eq("code-" + userId),
                eq(MILLIS_IN_A_DAY * 30));
        verify(userStateCache).setState(userId, State.NONE);
        verify(userStateCache).clearParamsForUser(userId);
        assertEquals("Кодовое слово успешно установлено", response.message());
    }

    /**
     * Тест, что не удалось установить пользователю кодовое слово, ведь он не был найден
     * и был отправлен верный ответ и статус
     *
     * @throws UserNotFoundException - ошибка, если не удалось найти пользователя
     */
    @Test
    void handle_userNotFoundException() throws Exception {
        long userId = 1L;
        String[] splitCommand = {"/code", "phrase"};

        doThrow(new UserNotFoundException("message")).when(userService).addCodeWordForUser(userId, "phrase");

        Response response = handler.handle(splitCommand, userId);

        verify(userStateCache).clearParamsForUser(userId);
        assertEquals("Пользователь не найден", response.message());
    }

    /**
     * Тест, что пользователю не удалось установить кодовое слово и вызвалась ошибка UserCodePhraseException
     *
     * @throws UserCodePhraseException - ошибка, если не удалось установить кодовое слово
     */
    @Test
    void handle_userCodePhraseException() throws Exception {
        long userId = 1L;
        String[] splitCommand = {"/code", "phrase"};

        doThrow(new UserCodePhraseException("123")).when(userService).addCodeWordForUser(userId, "phrase");

        Response response = handler.handle(splitCommand, userId);

        verify(userStateCache).clearParamsForUser(userId);
        assertEquals("Для пользователя уже задано кодовое слово", response.message());
    }

    /**
     * Тест, что произошла ошибка шифрования и
     * пользователь не смог установить кодовое слово и отправился верный ответ и статус
     *
     * @throws EncryptException - ошибка шифрования
     */
    @Test
    void handle_encryptException() throws Exception {
        long userId = 1L;
        String[] splitCommand = {"/code", "phrase"};

        doThrow(new EncryptException("Ошибка", new Exception())).when(userService).addCodeWordForUser(userId, "phrase");

        Response response = handler.handle(splitCommand, userId);

        verify(userStateCache).clearParamsForUser(userId);
        verify(userStateCache).setState(userId, State.NONE);
        assertEquals("Ошибка шифрования пароля", response.message());
    }
}
