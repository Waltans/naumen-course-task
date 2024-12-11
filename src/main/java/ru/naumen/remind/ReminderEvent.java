package ru.naumen.remind;

import org.springframework.context.ApplicationEvent;
import ru.naumen.bot.Response;

/**
 * Событие отправки напоминания
 */
public class ReminderEvent extends ApplicationEvent {

    /**
     * Id пользователя
     */
    private final String userId;

    /**
     * Ответ с напоминанием
     */
    private final Response response;

    public ReminderEvent(Object source, String userId, Response response) {
        super(source);
        this.userId = userId;
        this.response = response;
    }

    public String getUserId() {
        return userId;
    }

    public Response getResponse() {
        return response;
    }
}