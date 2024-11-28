package ru.naumen.bot.command;

import org.springframework.stereotype.Component;
import ru.naumen.exception.CommandNotFoundException;

/**
 * Класс для поиска команды и её свойств
 */
@Component
public class CommandFinder {

    /**
     * Поиск команды по текстовому вводу
     *
     * @param input строка ввода (например, "/edit" или "Изменить")
     * @return команда
     * @throws CommandNotFoundException когда команда не найдена
     */
    public Command findCommand(String input) throws CommandNotFoundException {
        for (Command cmd : Command.values()) {
            if (cmd.isCommandMatches(input)) {
                return cmd;
            }
        }
        throw new CommandNotFoundException("Команда не найдена: " + input);
    }
}

