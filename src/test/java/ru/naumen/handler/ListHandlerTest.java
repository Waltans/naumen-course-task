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
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.model.State.IN_LIST;
import static ru.naumen.model.State.NONE;

/**
 * Класс модульных тестов для ListHandler
 */
class ListHandlerTest {

    @Mock
    private EncodeService encodeService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserStateCache userStateCache;

    @InjectMocks
    private ListHandler listHandler;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест получения списка паролей, если у пользователя нет паролей
     */
    @Test
    void testGetUserPasswords_NoPasswords() {
        String[] command = {"/list"};
        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(List.of());

        Response response = listHandler.handle(command, 12345L);

        Assertions.assertEquals("Нет ни одного пароля. Справка: /help", response.message());
        Assertions.assertEquals(NONE, response.botState());
    }

    /**
     * Тест получения списка паролей, если у пользователя есть пароли
     */
    @Test
    void testGetUserPasswords_WithPasswords() {
        String[] command = {"/list"};
        UserPassword userPassword1 = new UserPassword("d1", "pass1", null);
        UserPassword userPassword2 = new UserPassword("d2", "pass2", null);
        List<UserPassword> userPasswords = List.of(userPassword1, userPassword2);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(encodeService.decryptData("pass1")).thenReturn("dpass1");
        Mockito.when(encodeService.decryptData("pass2")).thenReturn("dpass2");

        Response response = listHandler.handle(command, 12345L);
        String expectedMessage = String.format("\n%s) Сайт: %s, Пароль: %s", 1, "d1", "dpass1") +
                String.format("\n%s) Сайт: %s, Пароль: %s", 2, "d2", "dpass2");

        Assertions.assertEquals(expectedMessage, response.message());
        Assertions.assertEquals(IN_LIST, response.botState());
    }
}
