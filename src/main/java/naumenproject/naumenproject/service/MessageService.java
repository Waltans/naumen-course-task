package naumenproject.naumenproject.service;

import naumenproject.naumenproject.model.UserPassword;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис создания сообщений
 */
@Service
public class MessageService {

    private final EncodeService encodeService;

    public MessageService(EncodeService encodeService) {
        this.encodeService = encodeService;
    }

    /**
     * Создаёт приветственное сообщение
     */
    public String createWelcomeMessage() {
        return "Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.\n\n" +
                "Доступны следующие команды:\n" +
                "- /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);\n" +
                "- /save [password] [description] – Сохранить пароль, задать описание;\n" +
                "- /list – Показать список сохранённых паролей;\n" +
                "- /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;\n" +
                "- /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];\n" +
                "- /help - Справка.";
    }

    /**
     * Создаёт сообщение о генерации пароля
     * @param password пароль
     */
    public String createMessageWithPassword(String password) {
        return String.format("Сгенерирован пароль: %s", password);
    }

    /**
     * Создаёт сообщение об ошибке введённой длины
     */
    public String createMessageLengthError() {
        return "Длина пароля должна быть от 8 до 128 символов!";
    }

    /**
     * Создаёт сообщение об ошибке введённой сложности
     */
    public String createMessageComplexityError() {
        return "Сложность должна быть от 1 до 3, где:\n" +
                "1 - простой пароль;\n" +
                "2 - пароль средней сложности;\n" +
                "3 - сложный пароль.";
    }

    /**
     * Создаёт сообщение об ошибке не найденного пароля
     */
    public String createMessageNotFoundError(int id) {
        return String.format("Не найден пароль с id %s", id);
    }

    /**
     * Создаёт сообщение об успешном сохранении пароля
     */
    public String createMessagePasswordSaved() {
        return "Пароль успешно сохранён";
    }

    /**
     * Создаёт сообщение со списком паролей
     */
    public String createMessagePasswordList(List<UserPassword> userPasswords) {
        if (userPasswords.isEmpty()) {
            return "Нет ни одного пароля. Справка: /help";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userPasswords.size(); i++) {
            String description = userPasswords.get(i).getDescription();
            String password = encodeService.decryptData(userPasswords.get(i).getPassword());
            stringBuilder.append(String.format("\n%s) Сайт: %s, Пароль: %s", i + 1, description, password));
        }

        return stringBuilder.toString();
    }

    /**
     * Создаёт сообщение об удалении пароля
     * @param description описание пароля
     */
    public String createMessagePasswordDeleted(String description) {
        return String.format("Удалён пароль для сайта %s", description);
    }

    /**
     * Создаёт сообщение об обновлении пароля
     * @param description описание
     * @param password пароль
     */
    public String createMessagePasswordUpdated(String description, String password) {
        return String.format("Обновлён пароль для %s: %s", description, password);
    }
}
