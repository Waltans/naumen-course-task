package naumenproject.naumenproject;

import naumenproject.naumenproject.model.UserPassword;
import naumenproject.naumenproject.service.EncodeService;
import naumenproject.naumenproject.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Класс модульных тестов для MessageService
 */
class MessageServiceTest {

    @Mock
    private EncodeService encodeService;

    @InjectMocks
    private MessageService messageService;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест создания приветственного сообщения
     */
    @Test
    void testCreateWelcomeMessage() {
        String expectedMessage = "Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.\n\n" +
                "Доступны следующие команды:\n" +
                "- /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);\n" +
                "- /save [password] [description] – Сохранить пароль, задать описание;\n" +
                "- /list – Показать список сохранённых паролей;\n" +
                "- /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;\n" +
                "- /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];\n" +
                "- /help - Справка.";

        String result = messageService.createWelcomeMessage();
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения с паролем
     */
    @Test
    void testCreateMessageWithPassword() {
        String password = "testPassword";
        String expectedMessage = "Сгенерирован пароль: testPassword";

        String result = messageService.createMessageWithPassword(password);
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения об ошибке длины пароля
     */
    @Test
    void testCreateMessageLengthError() {
        String expectedMessage = "Длина пароля должна быть от 8 до 128 символов!";

        String result = messageService.createMessageLengthError();
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения об ошибке сложности пароля
     */
    @Test
    void testCreateMessageComplexityError() {
        String expectedMessage = "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";

        String result = messageService.createMessageComplexityError();
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения об ошибке при ненайденном пароле
     */
    @Test
    void testCreateMessageNotFoundError() {
        int id = 12345;
        String expectedMessage = "Не найден пароль с id 12345";

        String result = messageService.createMessageNotFoundError(id);
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения об успешном сохранении пароля
     */
    @Test
    void testCreateMessagePasswordSaved() {
        String expectedMessage = "Пароль успешно сохранён";

        String result = messageService.createMessagePasswordSaved();
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения со списком паролей
     */
    @Test
    void testCreateMessagePasswordList() {
        UserPassword password1 = new UserPassword();
        password1.setDescription("site1");
        password1.setPassword("encPass1");

        UserPassword password2 = new UserPassword();
        password2.setDescription("site2");
        password2.setPassword("encPass2");

        List<UserPassword> passwords = List.of(password1, password2);

        when(encodeService.decryptData("encPass1")).thenReturn("pass1");
        when(encodeService.decryptData("encPass2")).thenReturn("pass2");

        String result = messageService.createMessagePasswordList(passwords);

        assertTrue(result.contains("1) Сайт: site1, Пароль: pass1"));
        assertTrue(result.contains("2) Сайт: site2, Пароль: pass2"));
    }

    /**
     * Тест создания сообщения об удалении пароля
     */
    @Test
    void testCreateMessagePasswordDeleted() {
        String description = "site";
        String expectedMessage = "Удалён пароль для сайта site";

        String result = messageService.createMessagePasswordDeleted(description);
        assertEquals(expectedMessage, result);
    }

    /**
     * Тест создания сообщения об обновлении пароля
     */
    @Test
    void testCreateMessagePasswordUpdated() {
        String description = "site";
        String password = "newPass";
        String expectedMessage = "Обновлён пароль для site: newPass";

        String result = messageService.createMessagePasswordUpdated(description, password);
        assertEquals(expectedMessage, result);
    }
}
