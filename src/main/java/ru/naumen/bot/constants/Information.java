package ru.naumen.bot.constants;

/**
 * Константы - инфосообщения
 */
public class Information {
    public static final String WELCOME_MESSAGE = """
             Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.
                
             Доступны следующие команды:
             - /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);
             - /save [password] [description] [days] – Сохранить пароль, задать описание;
             - /list – Показать список сохранённых паролей;
             - /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;
             - /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];
             - /code [codePhrase] - Ввести кодовое слово для того чтобы можно было очистить несколько паролей по названию
             - /clear - команда которая очищает пароли у которых описание начинается с какого-то слова или буквы
             - /remind [passwordID] [days] - для того чтобы поставить напоминание через сколько обновить пароль
             - /help - Справка.
            """;

    public static final String PASSWORD_LIST_FORMAT = "%d) Сайт: %s, Пароль: %s";

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Information() {

    }
}
