package ru.naumen.bot.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.naumen.exception.CommandNotFoundException;

/**
 * Класс модульных тестов для CommandFinder
 */
class CommandFinderTest {

    private final CommandFinder commandFinder = new CommandFinder();

    /**
     * Тест поиска команды при корректной команде
     */
    @Test
    void testFindCommandCorrect() throws CommandNotFoundException {
        String input = "/edit";
        Command result = commandFinder.findCommand(input);

        Assertions.assertEquals(Command.EDIT, result);
    }

    /**
     * Тест поиска команды при корректной команде, ввод с клавиатуры
     */
    @Test
    void testFindCommandKeyboardLabel() throws CommandNotFoundException {
        String input = "Изменить";
        Command result = commandFinder.findCommand(input);

        Assertions.assertEquals(Command.EDIT, result);
    }

    /**
     * Тест поиска команды при некорректной команде
     */
    @Test
    void testFindCommandUnknown() {
        String input = "/unknown";
        CommandNotFoundException e = Assertions.assertThrows(
                CommandNotFoundException.class,
                () -> commandFinder.findCommand(input)
        );
        Assertions.assertEquals("Команда не найдена: /unknown", e.getMessage());
    }

    /**
     * Тест поиска команды при корректной команде и другим регистром
     */
    @Test
    void testFindCommandDifferentCase() throws CommandNotFoundException {
        String input = "/EDIT";

        Command result = commandFinder.findCommand(input);
        Assertions.assertEquals(Command.EDIT, result);
    }
}

