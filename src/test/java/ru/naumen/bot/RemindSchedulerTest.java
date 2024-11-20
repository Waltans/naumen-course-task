package ru.naumen.bot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Класс модульных тестов для RemindScheduler
 */
class RemindSchedulerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        remindScheduler.scheduleRemind("test", 12345L, 500);

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(eventCaptor.capture());
        Thread.sleep(500 + 20);

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());

        ReminderEvent capturedEvent = eventCaptor.getValue();
        Assertions.assertEquals("test", capturedEvent.getMessage());
    }

    /**
     * Тест планировки и отправки напоминания, если уже есть запланированные напоминания
     */
    @Test
    void testScheduleRemindMoreThanOne() throws InterruptedException {
        ArgumentCaptor<ReminderEvent> eventCaptor = ArgumentCaptor.forClass(ReminderEvent.class);

        remindScheduler.scheduleRemind("first", 12345L, 500);
        remindScheduler.scheduleRemind("second", 12345L, 1500);

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(eventCaptor.capture());
        Thread.sleep(500 + 20);
        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());
        ReminderEvent capturedEvent1 = eventCaptor.getValue();
        Assertions.assertEquals("first", capturedEvent1.getMessage());

        Thread.sleep(1000 + 20);
        Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(eventCaptor.capture());
        ReminderEvent capturedEvent2 = eventCaptor.getValue();
        Assertions.assertEquals("second", capturedEvent2.getMessage());
    }
}