package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.time.LocalDate;
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
    private KeyboardCreator keyboardCreator;

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
        User user = new User(12345L, List.of());
        List<UserPassword> userPasswords = List.of(
                new UserPassword("uuid", "desc", "pass", user, LocalDate.now()),
                new UserPassword("uuid2", "desc", "pass", user, LocalDate.now()));

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.isValidPasswordIndex(2, 12345L)).thenReturn(true);

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Удалён пароль для сайта desc", response.message());

        Mockito.verify(passwordService).deletePassword("uuid2");
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест удаления пароля с некорректным индексом
     */
    @Test
    void testDeletePassword_InvalidIndex() {
        String[] command = {"/del", "5"};
        User user = new User(12345L, List.of());
        List<UserPassword> userPasswords = List.of(new UserPassword("desc", "pass", user));

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Не найден пароль с id 5", response.message());

        Mockito.verify(passwordService, Mockito.never()).deletePassword(Mockito.anyString());
    }

    /**
     * Тест удаления пароля с не числовым индексом
     */
    @Test
    void testDeletePassword_NotNumberIndex() {
        String[] command = {"/del", "q"};
        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Индекс должен быть числом", response.message());
        Mockito.verify(passwordService, Mockito.never())
                .updatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    /**
     * Тест удаления, если команда введена с кнопки
     */
    @Test
    void testDeletePassword_WithoutParams() {
        String[] command = {"Удалить"};
        Mockito.when(userStateCache.getUserState(Mockito.anyLong())).thenReturn(State.NONE);
        Mockito.when(userStateCache.getUserParams(Mockito.anyLong())).thenReturn(List.of());

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Введите индекс пароля", response.message());
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testDeletePassword_InvalidCommand() {
        String[] command = {"/del", "1", "2", "3"};

        Response response = deleteHandler.handle(command, 12345L);

        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response.message());
    }
}
