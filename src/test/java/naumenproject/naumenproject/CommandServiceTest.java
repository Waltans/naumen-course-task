package naumenproject.naumenproject;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.model.UserPassword;
import naumenproject.naumenproject.service.CommandService;
import naumenproject.naumenproject.service.MessageService;
import naumenproject.naumenproject.service.PasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     * Тест комады /generate, при валидных значениях
     */
    @Test
    void testPerformCommandGenerate() {
        String command = "/generate 12 3";

        Mockito.when(passwordService.generatePasswordWithComplexity(12, 3)).thenReturn("generatedPassword");
        Mockito.when(messageService.createMessageWithPassword("generatedPassword")).thenReturn("Сгенерирован пароль: generatedPassword");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Сгенерирован пароль: generatedPassword", response);

        Mockito.verify(passwordService).generatePasswordWithComplexity(12, 3);
        Mockito.verify(messageService).createMessageWithPassword("generatedPassword");
    }

    /**
     * Тест комады /generate, если некорректно задана длина (ниже)
     */
    @Test
    void testPerformCommandGenerateLowLength() {
        String command = "/generate 4 3";

        Mockito.when(messageService.createMessageLengthError()).thenReturn("Длина пароля должна быть от 8 до 128 символов!");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);

        Mockito.verify(messageService).createMessageLengthError();
    }

    /**
     * Тест комады /generate, если некорректно задана длина (выше)
     */
    @Test
    void testPerformCommandGenerateHighLength() {
        String command = "/generate 129 3";

        Mockito.when(messageService.createMessageLengthError()).thenReturn("Длина пароля должна быть от 8 до 128 символов!");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);

        Mockito.verify(messageService).createMessageLengthError();
    }

    /**
     * Тест комады /generate, если некорректно задана сложность
     */
    @Test
    void testPerformCommandGenerateInvalidComplexity() {
        String command = "/generate 15 4";

        Mockito.when(messageService.createMessageComplexityError()).thenReturn("Сложность должна быть от 1 до 3...");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Сложность должна быть от 1 до 3...", response);

        Mockito.verify(messageService).createMessageComplexityError();
    }

    /**
     * Тест комады /save с описанием
     */
    @Test
    void testPerformCommandSaveWithDescription() {
        String command = "/save pass desc";

        Mockito.when(messageService.createMessagePasswordSaved()).thenReturn("Пароль успешно сохранён");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response);

        Mockito.verify(passwordService).createUserPassword("pass", "desc", 12345L);
        Mockito.verify(messageService).createMessagePasswordSaved();
    }

    /**
     * Тест комады /save без описания
     */
    @Test
    void testPerformCommandSaveWithoutDescription() {
        String command = "/save pass";

        Mockito.when(messageService.createMessagePasswordSaved()).thenReturn("Пароль успешно сохранён");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Пароль успешно сохранён", response);

        Mockito.verify(passwordService).createUserPassword("pass", "Неизвестно", 12345L);
        Mockito.verify(messageService).createMessagePasswordSaved();
    }

    /**
     * Тест комады /list
     */
    @Test
    void testPerformCommandList() {
        String command = "/list";

        List<UserPassword> userPasswords = List.of(new UserPassword(),
                new UserPassword());

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(messageService.createMessagePasswordList(userPasswords)).thenReturn("Список паролей");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Список паролей", response);

        Mockito.verify(passwordService).getUserPasswords(12345L);
        Mockito.verify(messageService).createMessagePasswordList(userPasswords);
    }

    /**
     * Тест команды /del, при валидных значениях
     */
    @Test
    void testPerformCommandDelete() {
        String command = "/del 1";

        String userUuid = UUID.randomUUID().toString();
        String passUuid = UUID.randomUUID().toString();
        User user = new User(userUuid, "name", 12345L, new ArrayList<>());
        UserPassword pass = new UserPassword(passUuid, "site", "pass", user);

        List<UserPassword> userPasswords = List.of(pass);

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(messageService.createMessagePasswordDeleted("site")).thenReturn("Удалён пароль для сайта site");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Удалён пароль для сайта site", response);

        Mockito.verify(passwordService).getUserPasswords(12345L);
        Mockito.verify(passwordService).deletePassword(passUuid);
        Mockito.verify(messageService).createMessagePasswordDeleted("site");
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(messageService.createMessageNotFoundError(2)).thenReturn("Не найден пароль с id 2");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response);

        Mockito.verify(passwordService).getUserPasswords(12345L);
        Mockito.verify(messageService).createMessageNotFoundError(2);
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(messageService.createMessageNotFoundError(-2)).thenReturn("Не найден пароль с id -2");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Не найден пароль с id -2", response);

        Mockito.verify(passwordService).getUserPasswords(12345L);
        Mockito.verify(messageService).createMessageNotFoundError(-2);
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.generatePasswordWithComplexity(12, 2)).thenReturn("newPass");
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        Mockito.when(messageService.createMessagePasswordUpdated("updDesc", "newPass"))
                .thenReturn("Обновлён пароль для updDesc: newPass");

        String response = commandService.performCommand(command, 12345L);

        Assertions.assertEquals("Обновлён пароль для updDesc: newPass", response);

        Mockito.verify(passwordService).getUserPasswords(12345L);
        Mockito.verify(passwordService).generatePasswordWithComplexity(12, 2);
        Mockito.verify(passwordService).updatePassword(passUuid, "updDesc", "newPass");
        Mockito.verify(messageService).createMessagePasswordUpdated("updDesc", "newPass");
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.generatePasswordWithComplexity(12, 2)).thenReturn("newPass");
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        Mockito.when(messageService.createMessagePasswordUpdated("site", "newPass"))
                .thenReturn("Обновлён пароль для site: newPass");

        String response = commandService.performCommand(command, 12345L);

        Assertions.assertEquals("Обновлён пароль для site: newPass", response);

        Mockito.verify(passwordService).getUserPasswords(12345L);
        Mockito.verify(passwordService).generatePasswordWithComplexity(12, 2);
        Mockito.verify(passwordService).updatePassword(passUuid, "site", "newPass");
        Mockito.verify(messageService).createMessagePasswordUpdated("site", "newPass");
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        Mockito.when(messageService.createMessageLengthError()).thenReturn("Длина пароля должна быть от 8 до 128 символов!");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Длина пароля должна быть от 8 до 128 символов!", response);

        Mockito.verify(messageService).createMessageLengthError();
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(passwordService.findPasswordByUuid(passUuid)).thenReturn(pass);
        Mockito.when(messageService.createMessageComplexityError()).thenReturn("Сложность должна быть от 1 до 3...");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Сложность должна быть от 1 до 3...", response);

        Mockito.verify(messageService).createMessageComplexityError();
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

        Mockito.when(passwordService.getUserPasswords(12345L)).thenReturn(userPasswords);
        Mockito.when(messageService.createMessageNotFoundError(2)).thenReturn("Не найден пароль с id 2");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Не найден пароль с id 2", response);

        Mockito.verify(messageService).createMessageNotFoundError(2);
    }

    /**
     * Тест комады /edit, введённой некорректно
     */
    @Test
    void testPerformCommandEditInvalidCommand() {
        String command = "/edit 2 10 2 15 14";

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response);
    }

    /**
     * Тест команды /help
     */
    @Test
    void testPerformCommandHelp() {
        String command = "/help";

        Mockito.when(messageService.createWelcomeMessage()).thenReturn("Здравствуйте. Я бот...");

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Здравствуйте. Я бот...", response);

        Mockito.verify(messageService).createWelcomeMessage();
    }

    /**
     * Тест невалидной команды
     */
    @Test
    void testPerformCommandInvalidCommand() {
        String command = "/invalid 123";

        String response = commandService.performCommand(command, 12345L);
        Assertions.assertEquals("Введена некорректная команда! Справка: /help", response);
    }
}
