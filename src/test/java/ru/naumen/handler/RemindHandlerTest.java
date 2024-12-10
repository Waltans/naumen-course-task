package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.model.State.REMIND_STEP_1;

/**
 * Класс модульных тестов для RemindHandler
 */
class RemindHandlerTest {

    @Mock
    private RemindScheduler remindScheduler;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private PasswordService passwordService;

    @Mock
    private KeyboardCreator keyboardCreator;

    @InjectMocks
    private RemindHandler remindHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест установки напоминания с корректными параметрами
     */
    @Test
    void testRemind_ValidParams() {
        String[] command = {"/remind", "1", "3"};
        List<UserPassword> userPasswords = List.of(
                new UserPassword("uuid", "desc", "pass", null, null)
        );
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.isValidPasswordIndex(1, 12345L)).thenReturn(true);
        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Напоминание для пароля desc установлено", response.message());

        Response remindResponse = new Response("Напоминание: обновите пароль для desc",
                keyboardCreator.createMainKeyboard());
        Mockito.verify(remindScheduler).scheduleRemind(12345L, "uuid", 259_200_000L, remindResponse);
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест ошибки, если пароль с указанным индексом не найден
     */
    @Test
    void testHandle_InvalidPasswordIndex() {
        String[] command = {"/remind", "10", "7"};

        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Не найден пароль с id 10", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест установки напоминания, если указано некорректное количество дней
     */
    @Test
    void testRemind_InvalidDays() {
        String[] command = {"Напомнить", "1", "-5"};

        Mockito.when(passwordService.isValidPasswordIndex(1, 12345L)).thenReturn(true);
        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Напоминание можно установить на срок от 3 до 90 дней", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест вызова установки напоминания
     */
    @Test
    void testRemind_WithoutParams() {
        String[] command = {"/remind"};

        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите индекс пароля", response.message());
        Mockito.verify(userStateCache).setState(12345L, REMIND_STEP_1);
    }
}
