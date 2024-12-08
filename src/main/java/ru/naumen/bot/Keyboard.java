package ru.naumen.bot;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * Клавиатура
 *
 * @param keyboardRows
 */
public record Keyboard(List<KeyboardRow> keyboardRows) {

}
