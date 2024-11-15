package ru.naumen.bot;

/**
 * Константы
 */
public class Constants {

    public static final String INCORRECT_COMMAND_RESPONSE = "Введена некорректная команда! Справка: /help";

    public static final String WELCOME_MESSAGE = "Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.\n\n" +
            "Доступны следующие команды:\n" +
            "- /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);\n" +
            "- /save [password] [description] – Сохранить пароль, задать описание;\n" +
            "- /list – Показать список сохранённых паролей;\n" +
            "- /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;\n" +
            "- /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];\n" +
            "- /help - Справка.";
    public static final String PASSWORD_GENERATED_MESSAGE = "Сгенерирован пароль: %s";

    public static final String LENGTH_ERROR_MESSAGE = "Длина пароля должна быть от 8 до 128 символов!";

    public static final String COMPLEXITY_ERROR_MESSAGE = "Сложность должна быть от 1 до 3, где:\n" +
            "1 - простой пароль;\n" +
            "2 - пароль средней сложности;\n" +
            "3 - сложный пароль.";
    public static final String PASSWORD_NOT_FOUND_MESSAGE = "Не найден пароль с id %s";

    public static final String PASSWORD_SAVED_MESSAGE = "Пароль успешно сохранён";

    public static final String NO_PASSWORDS_MESSAGE = "Нет ни одного пароля. Справка: /help";

    public static final String PASSWORD_LIST_FORMAT = "\n%s) Сайт: %s, Пароль: %s";

    public static final String PASSWORD_DELETED_MESSAGE = "Удалён пароль для сайта %s";

    public static final String PASSWORD_UPDATED_MESSAGE = "Обновлён пароль для %s: %s";

    public static final String ENTER_PASSWORD_LENGTH = "Введите длину пароля";

    public static final String ENTER_PASSWORD_COMPLEXITY = "Выберите сложность пароля";

    public static final String ENTER_PASSWORD_DESCRIPTION = "Введите описание пароля";

    public static final String ENTER_PASSWORD = "Введите пароль";
    public static final String CHOOSE_SORT_TYPE = "Отсортировать пароли по:";

    public static final String ENTER_PASSWORD_INDEX = "Введите индекс пароля";

    public static final String FAILURE = "Что-то пошло не так :( ";

    public static final String NO_PASSWORDS_FOUND = "Не найдены пароли по вашему запросу";
    public static final String ENTER_SEARCH_REQUEST = "Введите поисковый запрос";
    public static final String USER_NOT_FOUND = "Пользователь не найден";

    public static final String ENCRYPT_EXCEPTION = "Ошибка шифрования пароля";

    public static final String DECRYPT_EXCEPTION = "Ошибка дешифрования пароля";
    public static final int EDIT_COMMAND_LENGTH_HAS_DESCRIPTION = 5;
    public static final int SAVE_COMMAND_LENGTH_NO_DESCRIPTION = 2;
    public static final int COMMAND_WITHOUT_PARAMS_LENGTH = 1;
    public static int MINIMUM_PASSWORD_LENGTH = 8;
    public static int MAXIMUM_PASSWORD_LENGTH = 128;

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Constants() {

    }
}
