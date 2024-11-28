package ru.naumen.bot;

import ru.naumen.model.State;

/**
 * Ответ бота
 * @param message сообщение с ответом
 * @param botState состояние бота после ответа
 */
public record Response(String message, State botState) {
}
