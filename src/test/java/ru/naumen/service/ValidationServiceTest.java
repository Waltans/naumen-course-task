package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.command.CommandFinder;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;

/**
 * Класс модульных тестов для ValidationService
 */
class ValidationServiceTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    private ValidationService validationService;

    /**
     * Перед каждым тестом создаёт объекты (не моки!)
     * класса поиска команд и тестируемого класса
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CommandFinder commandFinder = new CommandFinder();
        validationService = new ValidationService(
                passwordService,
                userStateCache,
                commandFinder);
    }

    /**
     * Тест проверки валидности команды при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommand() {
        String[] splitCommand = {"/generate", "14", "3"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.NONE);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды с кнопки при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommandFromButton() {
        String[] splitCommand = {"Генерировать"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.NONE);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды в определённом состоянии при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommandInState() {
        String[] splitCommand = {"20"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды при невалидной команде
     */
    @Test
    void testIsValidCommand_InvalidCommand() {
        String[] splitCommand = {"/generate", "10", "abc"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности индекса пароля при корректном вводе
     */
    @Test
    void testIsValidPasswordIndex_ValidIndex() {
        Mockito.when(passwordService.countPasswordsByUserId(12345L)).thenReturn(5);

        Assertions.assertTrue(validationService.isValidPasswordIndex(12345L, 3));
    }

    /**
     * Тест проверки валидности индекса пароля при некорректном вводе
     */
    @Test
    void testIsValidPasswordIndex_InvalidIndex() {
        Mockito.when(passwordService.countPasswordsByUserId(12345L)).thenReturn(5);

        Assertions.assertFalse(validationService.isValidPasswordIndex(12345L, 10));
    }

    /**
     * Тест проверки валидности длины пароля при корректном вводе
     */
    @Test
    void testValidateLength_ValidParameter() {
        Assertions.assertTrue(validationService.isValidLength(12));
    }

    /**
     * Тест проверки валидности длины пароля при некорректном вводе (ниже)
     */
    @Test
    void testValidateLength_LowParameter() {
        Assertions.assertFalse(validationService.isValidLength(6));
    }

    /**
     * Тест проверки валидности длины пароля при некорректном вводе (выше)
     */
    @Test
    void testValidateLength_ValidParameters() {
        Assertions.assertFalse(validationService.isValidLength(130));
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
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SORT_STEP_1);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды в состоянии сортировки при некорректной команде
     */
    @Test
    void testIsValidCommand_SortInvalid() {
        String[] splitCommand = {"smth"};
        Mockito.when(userStateCache.getUserState(12345L)).thenReturn(State.SORT_STEP_1);

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }
}
