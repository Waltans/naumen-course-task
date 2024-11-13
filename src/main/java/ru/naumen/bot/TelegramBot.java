package ru.naumen.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.naumen.model.State;
import ru.naumen.service.CommandService;

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

    public TelegramBot(@Value("${bot.token}") String botToken, CommandService commandService, @Value("${bot.name}") String botName) {
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

            Response response = commandService.performCommand(messageText, userId, username);
            sendMessageToChat(response, chatId);
        }
    }

    /**
     * Отправляет сообщение в чат
     *
     * @param response - сообщение
     * @param id       - id чата, куда отправляем сообщение
     */
    private void sendMessageToChat(Response response, String id) {
        SendMessage tgMessage = new SendMessage();
        tgMessage.setText(response.message());
        tgMessage.setChatId(id);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        tgMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        if (response.botState().equals(State.NONE)) {
            List<KeyboardRow> keyboardRows = mainKeyboard();
            replyKeyboardMarkup.setKeyboard(keyboardRows);
        } else if (response.botState().equals(State.GENERATION_STEP_2)
                || response.botState().equals(State.EDIT_STEP_3)) {
            List<KeyboardRow> keyboardRows = complexityKeyBoard();
            replyKeyboardMarkup.setKeyboard(keyboardRows);
        } else {
            replyKeyboardMarkup.setKeyboard(List.of());
        }

        try {
            execute(tgMessage);
        } catch (TelegramApiException e) {
            log.error("Message could not be sent", e);
        }
    }

    private List<KeyboardRow> complexityKeyBoard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(Command.COMPLEXITY_1));
        keyboardRowFirst.add(new KeyboardButton(Command.COMPLEXITY_2));
        keyboardRowFirst.add(new KeyboardButton(Command.COMPLEXITY_3));

        keyboardRows.add(keyboardRowFirst);

        return keyboardRows;
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
        keyboardRowSecond.add(new KeyboardButton(Command.HELP_KEYBOARD));

        keyboardRows.add(keyboardRowFirst);
        keyboardRows.add(keyboardRowSecond);
        return keyboardRows;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}