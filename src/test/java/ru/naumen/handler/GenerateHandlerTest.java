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
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.model.State.*;

/**
 * Класс модульных тестов для GenerateHandler
 */
class GenerateHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private GenerateHandler generateHandler;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест генерации с корректными параметрами длины и сложности
     */
    @Test
    void testGeneratePassword_CorrectParameters() {
        Mockito.when(validationService.areNumbersGenerationCommandParams(Mockito.any(String[].class))).thenReturn(true);
        Mockito.when(passwordService.generatePassword(12, 3)).thenReturn("generatedPassword");

        String[] command = {"/generate", "12", "3"};
        Response response = generateHandler.generatePassword(command, 12345L);

        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест генерации, если длина пароля ниже минимального значения
     */
    @Test
    void testGeneratePassword_LowLength() {
        Mockito.when(validationService.areNumbersGenerationCommandParams(Mockito.any(String[].class))).thenReturn(true);

        String[] command = {"/generate", "4", "3"};
        Mockito.doThrow(new IllegalArgumentException("Длина пароля должна быть от 8 до 128 символов!"))
                .when(validationService).validateGenerationParameters(4, 3);

        Response response = generateHandler.generatePassword(command, 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест генерации, если длина пароля превышает максимальное значение
     */
    @Test
    void testGeneratePassword_HighLength() {
        Mockito.when(validationService.areNumbersGenerationCommandParams(Mockito.any(String[].class))).thenReturn(true);

        String[] command = {"/generate", "129", "3"};
        Mockito.doThrow(new IllegalArgumentException("Длина пароля должна быть от 8 до 128 символов!"))
                .when(validationService).validateGenerationParameters(129, 3);

        Response response = generateHandler.generatePassword(command, 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест генерации, если указана некорректная сложность
     */
    @Test
    void testGeneratePassword_InvalidComplexity() {
        Mockito.when(validationService.areNumbersGenerationCommandParams(Mockito.any(String[].class))).thenReturn(true);

        String[] command = {"/generate", "15", "4"};
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Mockito.doThrow(new IllegalArgumentException(expectedResponse))
                .when(validationService).validateGenerationParameters(15, 4);

        Response response = generateHandler.generatePassword(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест генерации, если передана некорректная команда
     */
    @Test
    void testGeneratePassword_InvalidCommand() {
        Mockito.when(validationService.areNumbersGenerationCommandParams(Mockito.any(String[].class))).thenReturn(false);

        String[] command = {"/generate", "ab", "4"};
        String expectedResponse = "Введена некорректная команда! Справка: /help";

        Response response = generateHandler.generatePassword(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест генерации, если передана команда с кнопки
     */
    @Test
    void testGeneratePassword_ButtonCommand() {
        String[] command = {"Генерировать"};
        String expectedResponse = "Введите длину пароля";

        Mockito.when(userStateCache.getTotalUserState())
                .thenReturn(new ConcurrentHashMap<>());
        Mockito.when(userStateCache.getTotalUserParams())
                .thenReturn(new ConcurrentHashMap<>());
        Response response = generateHandler.generatePassword(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
        Assertions.assertEquals(GENERATION_STEP_1, response.botState());
    }
}