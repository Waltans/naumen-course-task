package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;

import static org.mockito.Mockito.when;

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
        when(userStateCache.getUserState(12345L)).thenReturn(State.NONE);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды с кнопки при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommandFromButton() {
        String[] splitCommand = {"Генерировать"};
        when(userStateCache.getUserState(12345L)).thenReturn(State.NONE);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды в определённом состоянии при корректной команде
     */
    @Test
    void testIsValidCommand_ValidCommandInState() {
        String[] splitCommand = {"20"};
        when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды при невалидной команде
     */
    @Test
    void testIsValidCommand_InvalidCommand() {
        String[] splitCommand = {"/generate", "10", "abc"};
        when(userStateCache.getUserState(12345L)).thenReturn(State.GENERATION_STEP_1);

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
     * Тест проверки валидности дней до напоминания при корректном вводе
     */
    @Test
    void testValidateRemindDays_ValidParameter() {
        Assertions.assertTrue(validationService.isValidDays(12));
    }

    /**
     * Тест проверки валидности дней до напоминания пароля при некорректном вводе (ниже)
     */
    @Test
    void testValidateRemindDays_LowParameter() {
        Assertions.assertFalse(validationService.isValidDays(2));
    }

    /**
     * Тест проверки валидности дней до напоминания при некорректном вводе (выше)
     */
    @Test
    void testValidateRemindDays_ValidParameters() {
        Assertions.assertFalse(validationService.isValidDays(91));
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
        when(userStateCache.getUserState(12345L)).thenReturn(State.SORT_STEP_1);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест проверки валидности команды в состоянии сортировки при некорректной команде
     */
    @Test
    void testIsValidCommand_SortInvalid() {
        String[] splitCommand = {"smth"};
        when(userStateCache.getUserState(12345L)).thenReturn(State.SORT_STEP_1);

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест, что команда введена корректно
     */
    @Test
    void testIsValidCommand_codeValid() {
        String[] splitCommand = {"smth"};
        when(userStateCache.getUserState(12345L)).thenReturn(State.CODE_PHRASE_1);

        Assertions.assertTrue(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест, что у слова некорректная длина
     */
    @Test
    void testIsValidCommand_codeInvalidLength() {
        String[] splitCommand = {"a".repeat(51)};
        when(userStateCache.getUserState(12345L)).thenReturn(State.CODE_PHRASE_1);

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }

    /**
     * Тест, что слово не состоит из пробелов
     */
    @Test
    void testIsValidCommand_codeInvalid() {
        String[] splitCommand = {"   "};
        when(userStateCache.getUserState(12345L)).thenReturn(State.CODE_PHRASE_1);

        Assertions.assertFalse(validationService.isValidCommand(splitCommand, 12345L));
    }
}
