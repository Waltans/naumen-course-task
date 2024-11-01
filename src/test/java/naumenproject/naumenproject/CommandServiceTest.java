package naumenproject.naumenproject;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.model.UserPassword;
import naumenproject.naumenproject.service.CommandService;
import naumenproject.naumenproject.service.MessageService;
import naumenproject.naumenproject.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Класс модульных тестов для CommandService
 */
class CommandServiceTest {

    @Mock
    private MessageService messageService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private CommandService commandService;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест комады /generate
     */
    @Test
    void testPerformCommandGenerate() {
        String command = "/generate 12 3";

        when(passwordService.generatePassword(12, 3)).thenReturn("generatedPassword");
        when(messageService.createMessageWithPassword("generatedPassword")).thenReturn("Сгенерирован пароль: generatedPassword");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Сгенерирован пароль: generatedPassword", response);

        verify(passwordService).generatePassword(12, 3);
        verify(messageService).createMessageWithPassword("generatedPassword");
    }

    /**
     * Тест комады /generate, если некорректно задана длина (ниже)
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        String command = "/generate 4 3";

        when(messageService.createMessageLengthError()).thenReturn("Длина пароля должна быть от 8 до 128 символов!");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);

        verify(messageService).createMessageLengthError();
    }

    /**
     * Тест комады /generate, если некорректно задана длина (выше)
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        String command = "/generate 129 3";

        when(messageService.createMessageLengthError()).thenReturn("Длина пароля должна быть от 8 до 128 символов!");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);

        verify(messageService).createMessageLengthError();
    }

    /**
     * Тест комады /generate, если некорректно задана сложность
     */
    @Test
    void testPerformCommandGenerateInvalidComplexity() {
        String command = "/generate 15 4";

        when(messageService.createMessageComplexityError()).thenReturn("Сложность должна быть от 1 до 3...");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Сложность должна быть от 1 до 3...", response);

        verify(messageService).createMessageComplexityError();
    }

    /**
     * Тест комады /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        String command = "/save pass desc";

        when(messageService.createMessagePasswordSaved()).thenReturn("Пароль успешно сохранён");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Пароль успешно сохранён", response);

        verify(passwordService).createUserPassword("pass", "desc", 12345L);
        verify(messageService).createMessagePasswordSaved();
    }

    /**
     * Тест комады /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        String command = "/save pass";

        when(messageService.createMessagePasswordSaved()).thenReturn("Пароль успешно сохранён");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Пароль успешно сохранён", response);

        verify(passwordService).createUserPassword("pass", "Неизвестно", 12345L);
        verify(messageService).createMessagePasswordSaved();
    }

    /**
     * Тест комады /list
     */
    @Test
    void testPerformCommandList() {
        String command = "/list";

        List<UserPassword> userPasswords = List.of(new UserPassword(),
                new UserPassword());

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(messageService.createMessagePasswordList(userPasswords)).thenReturn("Список паролей");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Список паролей", response);

        verify(passwordService).getUserPasswords(12345L);
        verify(messageService).createMessagePasswordList(userPasswords);
    }

    /**
     * Тест комады /del
     */
    @Test
    void testPerformCommandDelete() {
        String command = "/del 1";

        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(messageService.createMessagePasswordDeleted("site")).thenReturn("Удалён пароль для сайта site");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Удалён пароль для сайта site", response);

        verify(passwordService).getUserPasswords(12345L);
        verify(passwordService).deletePassword(passUuid);
        verify(messageService).createMessagePasswordDeleted("site");
    }

    /**
     * Тест комады /del, если пароль по id не найден
     */
    @Test
    void testPerformCommandDeleteInvalidId() {
        String command = "/del 2";

        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(messageService.createMessageNotFoundError(2)).thenReturn("Не найден пароль с id 2");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Не найден пароль с id 2", response);

        verify(passwordService).getUserPasswords(12345L);
        verify(messageService).createMessageNotFoundError(2);
    }

    /**
     * Тест комады /del, если задан отрицательный id
     */
    @Test
    void testPerformCommandDeleteMinusId() {
        String command = "/del -2";

        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(messageService.createMessageNotFoundError(-2)).thenReturn("Не найден пароль с id -2");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Не найден пароль с id -2", response);

        verify(passwordService).getUserPasswords(12345L);
        verify(messageService).createMessageNotFoundError(-2);
    }

    /**
     * Тест комады /edit
     */
    @Test
    void testPerformCommandEditValid() {
        String command = "/edit 1 12 2 updDesc";
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(passwordService.generatePassword(12, 2)).thenReturn("newPass");
        when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        when(messageService.createMessagePasswordUpdated("updDesc", "newPass"))
                .thenReturn("Обновлён пароль для updDesc: newPass");

        String response = commandService.performCommand(command, 12345L);

        assertEquals("Обновлён пароль для updDesc: newPass", response);

        verify(passwordService).getUserPasswords(12345L);
        verify(passwordService).generatePassword(12, 2);
        verify(passwordService).updatePassword(passUuid, "updDesc", "newPass");
        verify(messageService).createMessagePasswordUpdated("updDesc", "newPass");
    }

    /**
     * Тест комады /edit, если не задано описание
     */
    @Test
    void testPerformCommandEditValidWithoutDescription() {
        String command = "/edit 1 12 2";
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(passwordService.generatePassword(12, 2)).thenReturn("newPass");
        when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        when(messageService.createMessagePasswordUpdated("site", "newPass"))
                .thenReturn("Обновлён пароль для site: newPass");

        String response = commandService.performCommand(command, 12345L);

        assertEquals("Обновлён пароль для site: newPass", response);

        verify(passwordService).getUserPasswords(12345L);
        verify(passwordService).generatePassword(12, 2);
        verify(passwordService).updatePassword(passUuid, "site", "newPass");
        verify(messageService).createMessagePasswordUpdated("site", "newPass");
    }

    /**
     * Тест комады /edit, если некорректно задана длина
     */
    @Test
    void testPerformCommandEditInvalidLength() {
        String command = "/edit 1 129 2 updDesc";
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        when(messageService.createMessageLengthError()).thenReturn("Длина пароля должна быть от 8 до 128 символов!");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);

        verify(messageService).createMessageLengthError();
    }

    /**
     * Тест комады /edit, если некорректно задана сложность
     */
    @Test
    void testPerformCommandEditInvalidComplexity() {
        String command = "/edit 1 12 4 updDesc";
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        when(messageService.createMessageComplexityError()).thenReturn("Сложность должна быть от 1 до 3...");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Сложность должна быть от 1 до 3...", response);

        verify(messageService).createMessageComplexityError();
    }

    /**
     * Тест комады /edit, если пароль по id не найден
     */
    @Test
    void testPerformCommandEditPasswordNotFound() {
        String command = "/edit 2 10 2 updDesc";
        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();

        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);
        List<UserPassword> userPasswords = List.of(pass);

        when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        when(messageService.createMessageNotFoundError(2)).thenReturn("Не найден пароль с id 2");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Не найден пароль с id 2", response);

        verify(messageService).createMessageNotFoundError(2);
    }

    /**
     * Тест комады /edit, введённой некорректно
     */
    @Test
    void testPerformCommandEditInvalidCommand() {
        String command = "/edit 2 10 2 15 14";

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Введена некорректная команда! Справка: /help", response);
    }

    /**
     * Тест команды /help
     */
    @Test
    void testPerformCommandHelp() {
        String command = "/help";

        when(messageService.createWelcomeMessage()).thenReturn("Здравствуйте. Я бот...");

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Здравствуйте. Я бот...", response);

        verify(messageService).createWelcomeMessage();
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testPerformCommandInvalidCommand() {
        String command = "/invalid 123";

        String response = commandService.performCommand(command, 12345L);
        assertEquals("Введена некорректная команда! Справка: /help", response);
    }
}
