package ru.naumen.bot;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * Клавиатура
 *
 * @param keyboardRows
 */
public record Keyboard(List<KeyboardRow> keyboardRows) {

    /**
     * Добавить новую строку на клавиатуру
     */
    public void addKeyboardRow() {
        this.keyboardRows.add(new KeyboardRow());
    }

    /**
     * Добавить новую кнопку на последнюю добавленную строку клавиатуры
     *
     * @param buttonText - текст кнопки
     */
    public void addButtonForLastKeyboardRow(String buttonText) {
        this.keyboardRows.getLast().add(new KeyboardButton(buttonText));
    }
}
