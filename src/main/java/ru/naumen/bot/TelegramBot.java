package ru.naumen.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.naumen.bot.command.Command;
import ru.naumen.model.State;
import ru.naumen.service.CommandService;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.constants.Parameters.*;

/**
 * Телеграм бот
 */
@Component
class TelegramBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private final CommandService commandService;
    private final String botName;

    public TelegramBot(@Value("${bot.token}") String botToken,
                       CommandService commandService,
                       @Value("${bot.name}") String botName) {
        super(botToken);
        this.commandService = commandService;
        this.botName = botName;
    }

    /**
     * Метод инициализации бота, выполняется после поднятия контекста
     */
    @EventListener({ContextRefreshedEvent.class})
    public void initialize() {
        try{
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Error initializing Bot", e);
            System.exit(1);
        }
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

            Response response = commandService.performCommand(messageText, userId);
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
        } else if (response.botState().equals(State.SORT_STEP_1)) {
            List<KeyboardRow> keyboardRows = sortKeyBoard();
            replyKeyboardMarkup.setKeyboard(keyboardRows);
        } else if (response.botState().equals(State.IN_LIST)) {
            List<KeyboardRow> keyboardRows = listKeyBoard();
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

    /**
     * Клавиатура с выбором сложности
     * Варианты:
     * Простой (COMPLEXITY_EASY)
     * Средний (COMPLEXITY_MEDIUM),
     * Сложный (COMPLEXITY_HARD)
     */
    private List<KeyboardRow> complexityKeyBoard() {
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
    private List<KeyboardRow> sortKeyBoard() {
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
    private List<KeyboardRow> listKeyBoard() {
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
    private List<KeyboardRow> mainKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(Command.GENERATE.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.SAVE.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.LIST.getKeyboardLabel()));
        keyboardRowFirst.add(new KeyboardButton(Command.HELP.getKeyboardLabel()));

        keyboardRows.add(keyboardRowFirst);
        return keyboardRows;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}