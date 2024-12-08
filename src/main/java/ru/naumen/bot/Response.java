package ru.naumen.bot;

/**
 * Ответ бота
 *
 * @param message сообщение с ответом
 */
public record Response(
        String message,
        Keyboard keyboard
) {
}
