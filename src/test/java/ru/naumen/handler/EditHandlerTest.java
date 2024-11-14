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
import ru.naumen.exception.PasswordNotFoundException;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Класс модульных тестов для EditHandler
 */
class EditHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private EditHandler editHandler;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест обновления пароля с корректными параметрами и описанием
     */
    @Test
    void testUpdatePassword_WithCorrectParamsAndDescription() throws PasswordNotFoundException {
        String[] command = {"/edit", "1", "12", "3", "newd"};
        UserPassword password = new UserPassword("uuid", "d", "pass", null, LocalDate.of(2010, 1, 1));
        List<UserPassword> userPasswords = List.of(password);

        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);
        Mockito.when(passwordService.generatePassword(12, 3)).thenReturn("npass");
        Mockito.when(passwordService.findPasswordByUuid("uuid")).thenReturn(password);

        Response response = editHandler.updatePassword(command, 12345L);

        Assertions.assertEquals("Обновлён пароль для newd: npass", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест обновления пароля с корректными параметрами без описания
     */
    @Test
    void testUpdatePassword_WithCorrectParamsWithoutDescription() throws PasswordNotFoundException {
        String[] command = {"/edit", "1", "12", "3"};
        UserPassword password = new UserPassword("uuid", "d", "pass", null, LocalDate.of(2010, 1, 1));
        List<UserPassword> userPasswords = List.of(password);

        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);
        Mockito.when(passwordService.generatePassword(12, 3)).thenReturn("npass");
        Mockito.when(passwordService.findPasswordByUuid("uuid")).thenReturn(password);

        Response response = editHandler.updatePassword(command, 12345L);

        Assertions.assertEquals("Обновлён пароль для d: npass", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест обновления пароля с некорректным индексом
     */
    @Test
    void testUpdatePassword_InvalidIndex() {
        String[] command = {"/edit", "5", "12", "3"};
        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(true);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 5)).thenReturn(false);

        Response response = editHandler.updatePassword(command, 12345L);

        Assertions.assertEquals("Не найден пароль с id 5", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест обновления, если команда введена с кнопки
     */
    @Test
    void testUpdatePassword_WithoutParams() {
        String[] command = {"Изменить"};
        Mockito.when(userStateCache.getTotalUserState()).thenReturn(new ConcurrentHashMap<>());
        Mockito.when(userStateCache.getTotalUserParams()).thenReturn(new ConcurrentHashMap<>());

        Response response = editHandler.updatePassword(command, 12345L);

        Assertions.assertEquals(ENTER_PASSWORD_INDEX, response.message());
        Assertions.assertEquals(EDIT_STEP_1, response.botState());
    }

    /**
     * Тест обновления, если команда некорректна
     */
    @Test
    void testUpdatePassword_InvalidCommand() {
        String[] command = {"/edit", "s"};
        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(false);

        Response response = editHandler.updatePassword(command, 12345L);

        Assertions.assertEquals(INCORRECT_COMMAND_RESPONSE, response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verifyNoInteractions(passwordService);
    }

    /**
     * Тест обновления с некорректными параметрами длины или сложности
     */
    @Test
    void testUpdatePassword_InvalidLengthOrComplexity() {
        String[] command = {"/edit", "1", "5", "4"};
        List<UserPassword> userPasswords = List.of(new UserPassword("desc", "pass", null));

        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);
        Mockito.doThrow(new IllegalArgumentException("Длина пароля должна быть от 8 до 128 символов!"))
                .when(validationService).validateGenerationParameters(5, 4);

        Response response = editHandler.updatePassword(command, 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }
}
