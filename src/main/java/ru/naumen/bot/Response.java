package ru.naumen.bot;

import ru.naumen.keyboard.Keyboard;

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
