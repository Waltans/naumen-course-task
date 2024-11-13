package ru.naumen;

import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.repository.UserRepository;
import ru.naumen.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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
    void testCreateUser() {
        long telegramId = 12345L;
        String name = "TestUser";

        userService.createUser(telegramId, name);

        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    /**
     * Тест получения объекта пользователя по Telegram ID
     */
    @Test
    void testGetUserByTelegramId_UserExists() throws UserNotFoundException {
        long telegramId = 12345L;
        User user = new User();
        user.setTelegramId(telegramId);
        user.setUsername("TestUser");

        Mockito.when(userRepository.findByTelegramId(telegramId)).thenReturn(user);

        User result = userService.getUserByTelegramId(telegramId);

        Assertions.assertEquals(user, result);
        Mockito.verify(userRepository, Mockito.times(1)).findByTelegramId(telegramId);
    }

    /**
     * Тест получения объекта пользователя по Telegram ID, когда такого не существует
     */
    @Test
    void testGetUserByTelegramId_UserNotFound() {
        long telegramId = 12345L;

        Mockito.when(userRepository.findByTelegramId(telegramId)).thenReturn(null);

        Exception exception = Assertions.assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByTelegramId(telegramId);
        });

        Assertions.assertEquals(String.format("Пользователь с id %s не найден", telegramId), exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1)).findByTelegramId(telegramId);
    }

    /**
     * Тест проверки существования пользователя по Telegram ID, если пользователь есть
     */
    @Test
    void testCheckUserExistsByTelegramId_UserExists() {
        long telegramId = 12345L;

        Mockito.when(userRepository.existsByTelegramId(telegramId)).thenReturn(true);

        boolean exists = userService.isUserExists(telegramId);

        Assertions.assertTrue(exists);
        Mockito.verify(userRepository, Mockito.times(1)).existsByTelegramId(telegramId);
    }

    /**
     * Тест проверки существования пользователя по Telegram ID, когда такого не существует
     */
    @Test
    void testCheckUserExistsByTelegramId_UserNotExists() {
        long telegramId = 12345L;

        Mockito.when(userRepository.existsByTelegramId(telegramId)).thenReturn(false);

        boolean exists = userService.isUserExists(telegramId);

        Assertions.assertFalse(exists);
        Mockito.verify(userRepository, Mockito.times(1)).existsByTelegramId(telegramId);
    }
}
