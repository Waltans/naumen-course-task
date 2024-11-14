package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;

import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Класс модульных тестов для ValidationService
 */
class ValidationServiceTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @InjectMocks
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест проверки валидности команды при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommand() {
        String[] splitCommand = {"/generate", "14", "3"};
        when(userStateCache.getTotalUserState()).thenReturn(Map.of(12345L, State.NONE));

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды с кнопки при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommandFromButton() {
        String[] splitCommand = {"Генерировать"};
        when(userStateCache.getTotalUserState()).thenReturn(Map.of(12345L, State.NONE));

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды в определённом состоянии при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommandInState() {
        String[] splitCommand = {"20"};
        when(userStateCache.getTotalUserState()).thenReturn(Map.of(12345L, State.GENERATION_STEP_1));

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды при невалидной команде
     */
    @Test
    void testIsValidCommand_InvalidCommand() {
        String[] splitCommand = {"/generate", "10", "abc"};
        when(userStateCache.getTotalUserState()).thenReturn(Map.of(12345L, State.GENERATION_STEP_1));

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности индекса пароля при корректном вводе
     */
    @Test
    void testIsValidPasswordIndex_ValidIndex() {
        when(passwordService.countPasswordsByUserId(12345L)).thenReturn(5);

        Assertions.assertTrue(validationService.isValidPasswordIndex(12345L, 3));
    }

    /**
     * Тест проверки валидности индекса пароля при некорректном вводе
     */
    @Test
    void testIsValidPasswordIndex_InvalidIndex() {
        when(passwordService.countPasswordsByUserId(12345L)).thenReturn(5);

        Assertions.assertFalse(validationService.isValidPasswordIndex(12345L, 10));
    }

    /**
     * Тест проверки валидности параметров генерации пароля при корректном вводе
     */
    @Test
    void testValidateGenerationParameters_ValidParameters() {
        Assertions.assertDoesNotThrow(() -> validationService.validateGenerationParameters(10, 2));
    }

    /**
     * Тест проверки валидности параметров генерации пароля при некорректном вводе длины
     */
    @Test
    void testValidateGenerationParameters_InvalidLength() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> validationService.validateGenerationParameters(5, 2));

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", exception.getMessage());
    }

    /**
     * Тест проверки валидности параметров генерации пароля при некорректном вводе сложности
     */
    @Test
    void testValidateGenerationParameters_InvalidComplexity() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> validationService.validateGenerationParameters(10, 5));

        String expectedMessage = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    /**
     * Тест проверки, что параметры редактирования пароля явл. числами при корректном вводе
     */
    @Test
    void testAreNumbersEditCommandParams_ValidNumbers() {
        String[] splitCommand = {"/edit", "1", "2", "3"};
        Assertions.assertTrue(validationService.areNumbersEditCommandParams(splitCommand));
    }

    /**
     * Тест проверки, что параметры редактирования пароля явл. числами при некорректном вводе
     */
    @Test
    void testAreNumbersEditCommandParams_InvalidNumbers() {
        String[] splitCommand = {"/edit", "1", "two", "3"};
        Assertions.assertFalse(validationService.areNumbersEditCommandParams(splitCommand));
    }

    /**
     * Тест проверки, что параметры удаления пароля явл. числами при корректном вводе
     */
    @Test
    void testAreNumbersDeleteCommandParams_ValidNumber() {
        String[] splitCommand = {"/del", "1"};
        Assertions.assertTrue(validationService.areNumbersDeleteCommandParams(splitCommand));
    }

    /**
     * Тест проверки, что параметры удаления пароля явл. числами при некорректном вводе
     */
    @Test
    void testAreNumbersDeleteCommandParams_InvalidNumber() {
        String[] splitCommand = {"/del", "one"};
        Assertions.assertFalse(validationService.areNumbersDeleteCommandParams(splitCommand));
    }

    /**
     * Тест проверки, что параметры генерации пароля явл. числами при корректном вводе
     */
    @Test
    void testAreNumbersGenerationCommandParams_ValidNumbers() {
        String[] splitCommand = {"/generate", "10", "2"};
        Assertions.assertTrue(validationService.areNumbersGenerationCommandParams(splitCommand));
    }

    /**
     * Тест проверки, что параметры генерации пароля явл. числами при некорректном вводе
     */
    @Test
    void testAreNumbersGenerationCommandParams_InvalidNumbers() {
        String[] splitCommand = {"/generate", "ten", "2"};
        Assertions.assertFalse(validationService.areNumbersGenerationCommandParams(splitCommand));
    }

    /**
     * Тест проверки валидности команды в состоянии сортировки при корректной команде
     */
    @Test
    void testIsValidCommand_Sort() {
        String[] splitCommand = {"Дате"};
        when(userStateCache.getTotalUserState()).thenReturn(Map.of(12345L, State.SORT_STEP_1));

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды в состоянии сортировки при некорректной команде
     */
    @Test
    void testIsValidCommand_SortInvalid() {
        String[] splitCommand = {"smth"};
        when(userStateCache.getTotalUserState()).thenReturn(Map.of(12345L, State.SORT_STEP_1));

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }
}
