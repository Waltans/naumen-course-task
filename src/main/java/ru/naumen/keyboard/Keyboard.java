package ru.naumen.keyboard;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * Клавиатура
 *
 * @param keyboardRows - строки клавиатуры
 */
public record Keyboard(List<KeyboardRow> keyboardRows) {

}
