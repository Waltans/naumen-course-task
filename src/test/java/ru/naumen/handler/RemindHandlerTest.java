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
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.List;
import static ru.naumen.model.State.NONE;
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
    private ValidationService validationService;

    @Mock
    private PasswordService passwordService;

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
        String[] command = {"/remind", "1", "1"};
        List<UserPassword> userPasswords = List.of(
                new UserPassword("uuid", "desc", "pass", null, null)
        );

        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);
        Mockito.when(validationService.isValidDays(1)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Напоминание для пароля desc установлено", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(remindScheduler).scheduleRemind("Напоминание: обновите пароль для desc", 12345L, 86_400_000L);
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест ошибки, если пароль с указанным индексом не найден
     */
    @Test
    void testHandle_InvalidPasswordIndex() {
        String[] command = {"/remind", "10", "7"};

        Mockito.when(validationService.isValidPasswordIndex(12345L, 10)).thenReturn(false);

        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Не найден пароль с id 10", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест установки напоминания, если указано некорректное количество дней
     */
    @Test
    void testRemind_InvalidDays() {
        String[] command = {"Напомнить", "1", "-5"};

        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);
        Mockito.when(validationService.isValidDays(-5)).thenReturn(false);

        Response response = remindHandler.handle(command, 12345L);

        Assertions.assertEquals("Напоминание можно установить на срок от 3 до 90 дней", response.message());
        Assertions.assertEquals(NONE, response.botState());
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
        Assertions.assertEquals(REMIND_STEP_1, response.botState());
        Mockito.verify(userStateCache).setState(12345L, REMIND_STEP_1);
    }
}
