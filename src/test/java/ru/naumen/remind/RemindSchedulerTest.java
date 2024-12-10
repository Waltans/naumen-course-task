package ru.naumen.remind;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import ru.naumen.bot.Response;
import ru.naumen.keyboard.KeyboardCreator;

/**
 * Класс модульных тестов для RemindScheduler
 */
class RemindSchedulerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private KeyboardCreator keyboardCreator;

    @InjectMocks
    private RemindScheduler remindScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест планировки и отправки напоминания
     */
    @Test
    void testScheduleRemind() throws InterruptedException {
        ArgumentCaptor<ReminderEvent> eventCaptor = ArgumentCaptor.forClass(ReminderEvent.class);
        Response remindResponse = new Response("test",
                keyboardCreator.createMainKeyboard());
        remindScheduler.scheduleRemind(12345L, "uuid", 500, remindResponse);

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(eventCaptor.capture());
        Thread.sleep(500 + 20);

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        ReminderEvent capturedEvent = eventCaptor.getValue();
        Assertions.assertEquals("test", capturedEvent.getResponse().message());
    }

    /**
     * Тест планировки и отправки напоминания, если уже есть запланированные напоминания
     */
    @Test
    void testScheduleRemindMoreThanOne() throws InterruptedException {
        ArgumentCaptor<ReminderEvent> eventCaptor = ArgumentCaptor.forClass(ReminderEvent.class);

        Response remindResponse1 = new Response("first",
                keyboardCreator.createMainKeyboard());
        Response remindResponse2 = new Response("second",
                keyboardCreator.createMainKeyboard());

        remindScheduler.scheduleRemind(12345L, "uuid", 500, remindResponse1);
        remindScheduler.scheduleRemind(12345L, "uuid1", 1500, remindResponse2);

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(eventCaptor.capture());
        Thread.sleep(500 + 20);
        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());
        ReminderEvent capturedEvent1 = eventCaptor.getValue();
        Assertions.assertEquals("first", capturedEvent1.getResponse().message());

        Thread.sleep(1000 + 20);
        Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(eventCaptor.capture());
        ReminderEvent capturedEvent2 = eventCaptor.getValue();
        Assertions.assertEquals("second", capturedEvent2.getResponse().message());
    }

    /**
     * Тест отмены напоминания
     */
    @Test
    void testCancelRemind() throws InterruptedException {
        Response remindResponse = new Response("test",
                keyboardCreator.createMainKeyboard());
        remindScheduler.scheduleRemind( 12345L, "uuid", 500, remindResponse);

        remindScheduler.cancelRemindIfScheduled("uuid");

        Thread.sleep(500 + 20);

        ArgumentCaptor<ReminderEvent> eventCaptor = ArgumentCaptor.forClass(ReminderEvent.class);
        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(eventCaptor.capture());
    }

    /**
     * Тест отмены напоминания после его выполнения
     */
    @Test
    void testCancelRemindAfterExecution() throws InterruptedException {
        Response remindResponse = new Response("test",
                keyboardCreator.createMainKeyboard());
        remindScheduler.scheduleRemind( 12345L, "uuid", 500, remindResponse);

        Thread.sleep(500 + 20);
        ArgumentCaptor<ReminderEvent> eventCaptor = ArgumentCaptor.forClass(ReminderEvent.class);
        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        remindScheduler.cancelRemindIfScheduled("uuid");
        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());
    }
}