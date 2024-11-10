package ru.naumen.initialize;

import ru.naumen.bot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Инициализатор бота
 */
@Component
class BotInitializer {

    private final Logger log = LoggerFactory.getLogger(BotInitializer.class);
    private final TelegramBot telegramBot;

    @Autowired
    public BotInitializer(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * Метод инициализации бота, выполняется после поднятия контекста
     */
    @EventListener({ContextRefreshedEvent.class})
    public void initialize() {
        try{
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            log.error("Error initializing Bot", e);
            System.exit(1);
        }
    }

}