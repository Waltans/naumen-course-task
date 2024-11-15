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
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

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


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест удаления пароля с корректным индексом
     */
    @Test
    void testDeletePassword_CorrectIndex() {
        String[] command = {"/del", "1"};
        User user = new User(12345L, new ArrayList<>());
        List<UserPassword> userPasswords = List.of(new UserPassword("desc", "pass", user));

        Mockito.when(validationService.areNumbersDeleteCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Удалён пароль для сайта desc", response.message());
        Assertions.assertEquals(NONE, response.botState());
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
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест удаления, если команда введена с кнопки
     */
    @Test
    void testDeletePassword_WithoutParams() {
        String[] command = {"Удалить"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals(ENTER_PASSWORD_INDEX, response.message());
        Assertions.assertEquals(DELETE_STEP_1, response.botState());
    }

    /**
     * Тест удаления, если команда некорректна
     */
    @Test
    void testDeletePassword_InvalidCommand() {
        String[] command = {"/del", "s"};
        Mockito.when(validationService.areNumbersDeleteCommandParams(command)).thenReturn(false);

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals(INCORRECT_COMMAND_RESPONSE, response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verifyNoInteractions(passwordService);
    }
}
