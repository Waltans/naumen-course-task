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
import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.ENTER_PASSWORD_INDEX;
import static ru.naumen.bot.Constants.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.model.State.EDIT_STEP_1;
import static ru.naumen.model.State.NONE;

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
        Mockito.when(validationService.isValidLength(12)).thenReturn(true);
        Mockito.when(validationService.isValidComplexity("3")).thenReturn(true);
        Mockito.when(passwordService.generatePassword(12, "3")).thenReturn("npass");
        Mockito.when(passwordService.findPasswordByUuid("uuid")).thenReturn(password);

        Response response = editHandler.handle(command, 12345L);

        Assertions.assertEquals("Обновлён пароль для newd: npass", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(passwordService).updatePassword("uuid", "newd", "npass");
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест обновления пароля с корректными параметрами без описания
     */
    @Test
    void testUpdatePassword_WithCorrectParamsWithoutDescription() throws PasswordNotFoundException {
        String[] command = {"/edit", "1", "12", "3"};
        UserPassword password = new UserPassword("uuid", "d", "pass", null,
                LocalDate.of(2010, 1, 1));
        List<UserPassword> userPasswords = List.of(password);

        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(true);
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 1)).thenReturn(true);
        Mockito.when(validationService.isValidLength(12)).thenReturn(true);
        Mockito.when(validationService.isValidComplexity("3")).thenReturn(true);
        Mockito.when(passwordService.generatePassword(12, "3")).thenReturn("npass");
        Mockito.when(passwordService.findPasswordByUuid("uuid")).thenReturn(password);

        Response response = editHandler.handle(command, 12345L);

        Assertions.assertEquals("Обновлён пароль для d: npass", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(passwordService).updatePassword("uuid", "d", "npass");
    }

    /**
     * Тест обновления пароля с некорректным индексом
     */
    @Test
    void testUpdatePassword_InvalidIndex() {
        String[] command = {"/edit", "5", "12", "3"};
        Mockito.when(validationService.areNumbersEditCommandParams(command)).thenReturn(true);
        Mockito.when(validationService.isValidPasswordIndex(12345L, 5)).thenReturn(false);

        Response response = editHandler.handle(command, 12345L);

        Assertions.assertEquals("Не найден пароль с id 5", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(passwordService, Mockito.never())
                .updatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Тест обновления, если команда введена с кнопки
     */
    @Test
    void testUpdatePassword_WithoutParams() {
        String[] command = {"Изменить"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(new ArrayList<>());

        Response response = editHandler.handle(command, 12345L);

        Assertions.assertEquals(ENTER_PASSWORD_INDEX, response.message());
        Assertions.assertEquals(EDIT_STEP_1, response.botState());
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

        Response response = editHandler.handle(command, 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(passwordService, Mockito.never())
                .updatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
