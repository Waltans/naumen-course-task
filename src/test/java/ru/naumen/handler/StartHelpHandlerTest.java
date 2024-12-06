package ru.naumen.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.UserService;

import static ru.naumen.model.State.NONE;

/**
 * Класс модульных тестов для StartHelpHandler
 */
class StartHelpHandlerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserStateCache userStateCache;

    @InjectMocks
    private StartHelpHandler startHelpHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест команды /start
     */
    @Test
    void testStartCommand() {
        String[] command = {"/start"};
        String expectedResult = """
                 Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.
                            
                 Доступны следующие команды:
                 - /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);
                 - /save [password] [description] [days] – Сохранить пароль, задать описание;
                 - /list – Показать список сохранённых паролей;
                 - /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;
                 - /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];
                 - /code [codePhrase] - Ввести кодовое слово для того чтобы можно было очистить несколько паролей по названию
                 - /clear - команда которая очищает пароли у которых описание начинается с какого-то слова или буквы
                 - /remind [passwordID] [days] - для того чтобы поставить напоминание через сколько обновить пароль
                 - /help - Справка.
                """;

        Mockito.doNothing().when(userService).createUserIfUserNotExists(12345L);
        Response response = startHelpHandler.handle(command, 12345L);

        Assertions.assertEquals(expectedResult, response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }

    /**
     * Тест команды /help
     */
    @Test
    void testHelpCommand() {
        String[] command = {"/help"};
        String expectedResult = """
                 Здравствуйте. Я бот, который поможет Вам генерировать и управлять паролями.
                            
                 Доступны следующие команды:
                 - /generate [length] [complexity] – Генерировать пароль длиной [length] символов и сложностью [complexity] (1, 2 или 3, где 1 - простой, 3 - сложный);
                 - /save [password] [description] [days] – Сохранить пароль, задать описание;
                 - /list – Показать список сохранённых паролей;
                 - /edit [passwordID] [length] [complexity] [description] – Изменяет пароль с ID [passwordID], генерирует новый под заданные параметры;
                 - /del [passwordID] – Удалить сохранённый пароль с ID [passwordID];
                 - /code [codePhrase] - Ввести кодовое слово для того чтобы можно было очистить несколько паролей по названию
                 - /clear - команда которая очищает пароли у которых описание начинается с какого-то слова или буквы
                 - /remind [passwordID] [days] - для того чтобы поставить напоминание через сколько обновить пароль
                 - /help - Справка.
                """;

        Response response = startHelpHandler.handle(command, 12345L);

        Assertions.assertEquals(expectedResult, response.message());
        Assertions.assertEquals(NONE, response.botState());
        Mockito.verify(userStateCache).clearParamsForUser(12345L);
    }
}