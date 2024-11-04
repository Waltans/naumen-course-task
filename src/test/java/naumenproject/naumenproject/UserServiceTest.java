package naumenproject.naumenproject;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.repository.UserRepository;
import naumenproject.naumenproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Тест получения объекта пользователя по Telegram ID
     */
    @Test
    void testGetUserByTelegramId_UserExists() {
        long telegramId = 12345L;
        User user = new User();
        user.setTelegramId(telegramId);
        user.setUsername("TestUser");

        when(userRepository.findByTelegramId(telegramId)).thenReturn(user);

        User result = userService.getUserByTelegramId(telegramId);

        assertEquals(user, result);
        verify(userRepository, times(1)).findByTelegramId(telegramId);
    }

    /**
     * Тест получения объекта пользователя по Telegram ID, когда такого не существует
     */
    @Test
    void testGetUserByTelegramId_UserNotFound() {
        long telegramId = 12345L;

        when(userRepository.findByTelegramId(telegramId)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByTelegramId(telegramId);
        });

        assertEquals(String.format("Пользователь с id %s не найден", telegramId), exception.getMessage());
        verify(userRepository, times(1)).findByTelegramId(telegramId);
    }

    /**
     * Тест проверки существования пользователя по Telegram ID, если пользователь есть
     */
    @Test
    void testCheckUserExistsByTelegramId_UserExists() {
        long telegramId = 12345L;

        when(userRepository.existsByTelegramId(telegramId)).thenReturn(true);

        boolean exists = userService.checkUserExistsByTelegramId(telegramId);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByTelegramId(telegramId);
    }

    /**
     * Тест проверки существования пользователя по Telegram ID, когда такого не существует
     */
    @Test
    void testCheckUserExistsByTelegramId_UserNotExists() {
        long telegramId = 12345L;

        when(userRepository.existsByTelegramId(telegramId)).thenReturn(false);

        boolean exists = userService.checkUserExistsByTelegramId(telegramId);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByTelegramId(telegramId);
    }
}
