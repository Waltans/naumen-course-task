package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.naumen.exception.UserCodePhraseException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.repository.UserRepository;

import java.time.LocalDate;

/**
 * Класс модульных тестов для UserService
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест создания объекта пользователя
     */
    @Test
    void testCreateUserIfUserNotExists() {
        long id = 12345L;

        userService.createUserIfUserNotExists(id);

        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    /**
     * Тест получения объекта пользователя по ID
     */
    @Test
    void testGetUserById_UserExists() throws UserNotFoundException {
        long id = 12345L;
        User user = new User(id);

        Mockito.when(userRepository.findById(id)).thenReturn(user);

        User result = userService.getUserById(id);

        Assertions.assertEquals(user, result);
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);
    }

    /**
     * Тест получения объекта пользователя по ID, когда такого не существует
     */
    @Test
    void testGetUserById_UserNotFound() {
        long id = 12345L;

        Mockito.when(userRepository.findById(id)).thenReturn(null);

        Exception exception = Assertions.assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(id);
        });

        Assertions.assertEquals(String.format("Пользователь с id %s не найден", id), exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);
    }

    /**
     * Устанавливаем кодовое слово, когда его не было до этого
     *
     * @throws UserCodePhraseException - ошибка, если не удалось поменять кодовое слово
     */
    @Test
    void setCodePhrase() throws UserCodePhraseException {
        User user = new User(12345L);
        user.setCodePhrase("newCodePhrase");

        Assertions.assertEquals("newCodePhrase", user.getCodePhrase());
        Assertions.assertEquals(LocalDate.now(), user.getCodeModifyDate());
    }

    /**
     * Тест, что мы устанавливаем кодовое слово, если прошло больше 30 дней
     *
     * @throws UserCodePhraseException - ошибка, если не удалось поменять кодовое слово
     */
    @Test
    void setCodePhraseCodePhraseOlder() throws UserCodePhraseException {
        User user = Mockito.spy(new User(12345L));
        user.setCodePhrase("oldCodePhrase");

        Mockito.when(user.getCodeModifyDate()).thenReturn(LocalDate.now().minusDays(31));

        user.setCodePhrase("newCodePhrase");

        Assertions.assertEquals("newCodePhrase", user.getCodePhrase());
    }

    /**
     * Тест, что падает ошибка, если мы пытаемся поменять кодовое слово раньше, чем за 30 дней
     *
     * @throws UserCodePhraseException - ошибка, если не удалось поменять кодовое слово
     */
    @Test
    void setCodePhrase_recentlyModified() throws UserCodePhraseException {
        User user = new User(12345L);
        user.setCodePhrase("initialCodePhrase");

        UserCodePhraseException exception = Assertions.assertThrows(UserCodePhraseException.class, () -> {
            user.setCodePhrase("newCodePhrase");
        });

        Assertions.assertEquals("Невозможно сменить кодовое слово", exception.getMessage());
        Assertions.assertEquals("initialCodePhrase", user.getCodePhrase());
    }
}