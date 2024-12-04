package ru.naumen.bot.keyboards;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.naumen.bot.command.Command;
import ru.naumen.model.State;
import ru.naumen.repository.UserStateCache;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.constants.Parameters.*;

@Service
public class KeyboardsService {

    private final UserStateCache userStateCache;

    public KeyboardsService(UserStateCache userStateCache) {
        this.userStateCache = userStateCache;
    }

    public List<KeyboardRow> getKeyboards(Integer userId) {
        State state = userStateCache.getUserState(userId);

        return switch (state) {
            case NONE -> createMainKeyboard();
            case GENERATION_STEP_2, EDIT_STEP_3 -> createComplexityKeyBoard();
            case SORT_STEP_1 -> createSortKeyboard();
            case IN_LIST -> createListKeyboard();
            default -> List.of();
        };
    }


    /**
     * Клавиатура с выбором сложности
     * Варианты:
     * Простой (COMPLEXITY_EASY)
     * Средний (COMPLEXITY_MEDIUM),
     * Сложный (COMPLEXITY_HARD)
     */
    private List<KeyboardRow> createComplexityKeyBoard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(COMPLEXITY_EASY));
        keyboardRowFirst.add(new KeyboardButton(COMPLEXITY_MEDIUM));
        keyboardRowFirst.add(new KeyboardButton(COMPLEXITY_HARD));

        keyboardRows.add(keyboardRowFirst);

        return keyboardRows;
    }

    /**
     * Клавиатура с выбором типа сортировки
     * Можно выбрать по дате (BY_DATE) и описанию (BY_DESCRIPTION)
     */
    private List<KeyboardRow> createSortKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(BY_DATE));
        keyboardRowFirst.add(new KeyboardButton(BY_DESCRIPTION));

        keyboardRows.add(keyboardRowFirst);

        return keyboardRows;
    }

    /**
     * Клавиатура в менеджере паролей
     * Кнопки:
     * MENU - возврат в главное меню
     * DELETE - начать процедуру удаления пароля
     * EDIT - начать процедуру изменения пароля
     * SORT - отсортировать пароли
     * FIND - поиск паролей по описанию
     */
    private List<KeyboardRow> createListKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(Command.START.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.DELETE.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.EDIT.getKeyboardLabel()));

        KeyboardRow keyboardRowSecond = new KeyboardRow();
        keyboardRowSecond.add(new KeyboardButton(Command.SORT.getKeyboardLabel()));
        keyboardRowSecond.add(new KeyboardButton(Command.FIND.getKeyboardLabel()));

        keyboardRows.add(keyboardRowFirst);
        keyboardRows.add(keyboardRowSecond);

        return keyboardRows;
    }

    /**
     * Клавиатура основная
     * Кнопки:
     * GENERATE - начать процедуру генерации пароля
     * SAVE - начать процедуру сохранения пароля
     * LIST - список паролей и переход к менеджеру (управление сохранёнными паролями)
     * HELP - справка по работе бота
     */
    private List<KeyboardRow> createMainKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(Command.GENERATE.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.SAVE.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.LIST.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.HELP.getKeyboardLabel()));

        keyboardRows.add(keyboardRowFirst);
        return keyboardRows;
    }
}
