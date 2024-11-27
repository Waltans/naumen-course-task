package ru.naumen.bot.constants;

/**
 * Константы - инфосообщения
 */
public class Information {
    public static final String WELCOME_MESSAGE = """
    Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.

    Доступны следующие команды:
    - /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);
    - /save [password] [description] – Сохранить пароль, задать описание;
    - /list – Показать список сохранённых паролей;
    - /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;
    - /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];
    - /help - Справка.""";
    public static final String PASSWORD_GENERATED_MESSAGE = "Сгенерирован пароль: %s";

    public static final String PASSWORD_SAVED_MESSAGE = "Пароль успешно сохранён";
    public static final String PASSWORD_LIST_FORMAT = "%d) Сайт: %s, Пароль: %s";
    public static final String PASSWORD_DELETED_MESSAGE = "Удалён пароль для сайта %s";
    public static final String PASSWORD_UPDATED_MESSAGE = "Обновлён пароль для %s: %s";

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Information() {

    }
}
