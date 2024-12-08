package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.keyboards.KeyboardCreator;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.ComplexityFormatException;
import ru.naumen.exception.PasswordLengthException;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.List;

/**
 * Класс модульных тестов для GenerateHandler
 */
class GenerateHandlerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @InjectMocks
    private GenerateHandler generateHandler;

    @Mock
    private KeyboardCreator keyboardCreator;


    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест генерации с корректными параметрами длины и сложности
     */
    @Test
    void testGeneratePassword_CorrectParameters() throws PasswordLengthException, ComplexityFormatException {
        Mockito.when(passwordService.generatePassword(12, "3")).thenReturn("generatedPassword");

        String[] command = {"/generate", "12", "3"};
        Response response = generateHandler.handle(command, 12345L);

        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response.message());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест генерации, если длина пароля ниже минимального значения
     */
    @Test
    void testGeneratePassword_LowLength() throws PasswordLengthException, ComplexityFormatException {
        String[] command = {"/generate", "4", "3"};

        Mockito.when(passwordService.generatePassword(4, "3")).thenThrow(PasswordLengthException.class);
        Response response = generateHandler.handle(command, 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
    }

    /**
     * Тест генерации, если длина пароля превышает максимальное значение
     */
    @Test
    void testGeneratePassword_HighLength() throws PasswordLengthException, ComplexityFormatException {
        String[] command = {"/generate", "129", "3"};

        Mockito.when(passwordService.generatePassword(129, "3")).thenThrow(PasswordLengthException.class);
        Response response = generateHandler.handle(command, 12345L);

        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response.message());
    }

    /**
     * Тест генерации, если указана некорректная сложность
     */
    @Test
    void testGeneratePassword_InvalidComplexity() throws PasswordLengthException, ComplexityFormatException {
        String[] command = {"/generate", "15", "4"};
        String expectedResponse = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        Mockito.when(passwordService.generatePassword(15, "4")).thenThrow(ComplexityFormatException.class);

        Response response = generateHandler.handle(command, 12345L);


        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест генерации, если передана команда с кнопки
     */
    @Test
    void testGeneratePassword_ButtonCommand() {
        String[] command = {"Генерировать"};
        String expectedResponse = "Введите длину пароля";

        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(State.NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());
        Response response = generateHandler.handle(command, 12345L);

        Assertions.assertEquals(expectedResponse, response.message());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testGeneratePassword_InvalidCommand() {
        String[] command = {"/generate", "1", "3", "1"};

        Response response = generateHandler.handle(command, 12345L);

        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }
}