package ru.naumen.bot;

import org.springframework.context.ApplicationEvent;

/**
 * Событие отправки напоминания
 */
public class ReminderEvent extends ApplicationEvent {

    /**
     * Id пользователя
     */
    private final String userId;

    /**
     * Сообщение с напоминанием
     */
    private final String message;

    public ReminderEvent(Object source, String userId, String message) {
        super(source);
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }
}