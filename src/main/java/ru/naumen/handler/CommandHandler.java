package ru.naumen.handler;

import ru.naumen.bot.Response;

/**
 * Хэндлер команд
 */
public interface CommandHandler {

    /**
     * Исполнить команду
     *
     * @param splitCommand команда, разделённая по пробелам
     * @param userId       Id пользователя
     * @return ответ
     */
    Response handle(String[] splitCommand, long userId);

    /**
     * Проверяем корректность введенной команды
     *
     * @return true, если команда корректна
     */
    boolean isValid(String[] command);
}
