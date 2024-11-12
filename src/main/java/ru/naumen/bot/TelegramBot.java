package ru.naumen.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.naumen.service.CommandService;
import ru.naumen.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис по принятию и отправки сообщений в бота
 */
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private final CommandService commandService;
    private final String botName;

    public TelegramBot(@Value("${bot.token}") String botToken, UserService userService, CommandService commandService, @Value("${bot.name}") String botName) {
        super(botToken);
        this.commandService = commandService;
        this.botName = botName;
    }

    /**
     * Обрабатывает полученное сообщение, создаёт нового пользователя,
     * если он впервые взаимодействует с ботом
     *
     * @param update обновление
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();

            String response = commandService.performCommand(messageText, userId, username);
            sendMessageToChat(response, chatId);
        }
    }

    /**
     * Отправляет сообщение в чат
     *
     * @param message - сообщение
     * @param id - id чата, куда отправляем сообщение
     */
    private void sendMessageToChat(String message, String id) {
        SendMessage tgMessage = new SendMessage();
        tgMessage.setText(message);
        tgMessage.setChatId(id);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        tgMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = mainKeyboard();

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        try {
            execute(tgMessage);
        } catch (TelegramApiException e) {
            log.error("Message could not be sent", e);
        }
    }

    private List<KeyboardRow> mainKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(Command.GENERATE_KEYBOARD));
        keyboardRowFirst.add(new KeyboardButton(Command.SAVE_KEYBOARD));
        keyboardRowFirst.add(new KeyboardButton(Command.LIST_KEYBOARD));

        KeyboardRow keyboardRowSecond = new KeyboardRow();
        keyboardRowSecond.add(new KeyboardButton(Command.DELETE_KEYBOARD));
        keyboardRowSecond.add(new KeyboardButton(Command.EDIT_KEYBOARD));
        keyboardRowSecond.add(new KeyboardButton(Command.HELP));

        keyboardRows.add(keyboardRowFirst);
        keyboardRows.add(keyboardRowSecond);
        return keyboardRows;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}