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
import ru.naumen.exception.UserCodePhraseException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.service.UserService;

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
    private RemindScheduler remindScheduler;
    @Mock
    private KeyboardCreator keyboardCreator;
    @InjectMocks
    private AddCodePhraseHandler addCodePhraseHandler;

    /**
     * Проверяем, что при получении команды без параметров
     * пользователю устанавливается статус и возвращается верный ответ
     */
    @Test
    void handleNoParams() {
        long userId = 1L;
        String[] splitCommand = {"/code"};

        Response response = addCodePhraseHandler.handle(splitCommand, userId);

        Mockito.verify(userStateCache).setState(userId, State.CODE_PHRASE_1);
        Assertions.assertEquals("Введите кодовое слово", response.message());
    }

    /**
     * Тест, что кодовое слово установилось успешно, установился таймер и
     * пользователю пришел верный ответ с правильным статусом
     */
    @Test
    void handleAddCodePhraseSuccessfully() throws Exception {
        long userId = 1L;
        String[] splitCommand = {"/code", "phrase"};

        Response response = addCodePhraseHandler.handle(splitCommand, userId);
        Response remindResponse = new Response("Вам необходимо заменить кодовое слово",
                keyboardCreator.createMainKeyboard());

        Mockito.verify(userService).addCodeWordForUser(userId, "phrase");
        Mockito.verify(remindScheduler).scheduleRemind(
                Mockito.eq(userId),
                Mockito.eq("code-1"),
                Mockito.eq(2_592_000_000L),
                Mockito.eq(remindResponse));
        Mockito.verify(userStateCache).setState(userId, State.NONE);
        Mockito.verify(userStateCache).clearParamsForUser(userId);
        Assertions.assertEquals("Кодовое слово успешно установлено", response.message());
    }

    /**
     * Тест, что пользователю не удалось установить кодовое слово, так как оно уже установлено
     */
    @Test
    void handle_userCodePhraseException() throws Exception {
        long userId = 1L;
        String[] splitCommand = {"/code", "phrase"};

        Mockito.doThrow(new UserCodePhraseException("123")).when(userService).addCodeWordForUser(userId, "phrase");

        Response response = addCodePhraseHandler.handle(splitCommand, userId);

        Mockito.verify(userStateCache).clearParamsForUser(userId);
        Assertions.assertEquals("Для пользователя уже задано кодовое слово", response.message());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testCode_InvalidCommand() {
        String[] command = {"/code", "1", "2", "3"};

        Response response = addCodePhraseHandler.handle(command, 12345L);

        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }
}
