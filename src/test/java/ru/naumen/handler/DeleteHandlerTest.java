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
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс модульных тестов для DeleteHandler
 */
class DeleteHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private DeleteHandler deleteHandler;


    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест удаления пароля с корректным индексом
     */
    @Test
    void testDeletePassword_CorrectIndex() {
        String[] command = {"/del", "2"};
        User user = new User(12345L, new ArrayList<>());
        List<UserPassword> userPasswords = List.of(
                new UserPassword("uuid", "desc", "pass", user, LocalDate.now()),
                new UserPassword("uuid2", "desc", "pass", user, LocalDate.now()));

        Mockito.when(validationService.areNumbersDeleteCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 2)).thenReturn(true);

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Удалён пароль для сайта desc", response.message());
        Assertions.assertEquals(State.NONE, response.botState());

        Mockito.verify(passwordService).deletePassword("uuid2");
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест удаления пароля с некорректным индексом
     */
    @Test
    void testDeletePassword_InvalidIndex() {
        String[] command = {"/del", "5"};
        User user = new User(12345L, new ArrayList<>());
        List<UserPassword> userPasswords = List.of(new UserPassword("desc", "pass", user));

        Mockito.when(validationService.areNumbersDeleteCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 5)).thenReturn(false);

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Не найден пароль с id 5", response.message());
        Assertions.assertEquals(State.NONE, response.botState());

        Mockito.verify(passwordService, Mockito.never()).deletePassword(Mockito.anyString());
    }

    /**
     * Тест удаления, если команда введена с кнопки
     */
    @Test
    void testDeletePassword_WithoutParams() {
        String[] command = {"Удалить"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(State.NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите индекс пароля", response.message());
        Assertions.assertEquals(State.DELETE_STEP_1, response.botState());
    }
}
