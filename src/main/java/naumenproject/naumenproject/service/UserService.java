package naumenproject.naumenproject.service;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Класс для работы с пользователями
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Создаёт пользователя и сохраняет в БД
     *
     * @param telegramId ID пользователя в telegram
     * @param name       имя пользоваетля
     */
    public void createUser(long telegramId, String name) {
        User user = new User.Builder()
                .username(name)
                .telegramId(telegramId)
                .userPasswords(new ArrayList<>())
                .build();

        userRepository.save(user);
        log.info("Создан пользователь с telegram id {}", telegramId);
    }

    /**
     * Возвращает пользователя по его Telegram ID.
     *
     * @param telegramId telegram ID пользователя
     */
    public User getUserByTelegramId(long telegramId) {
        User user = userRepository.findByTelegramId(telegramId);

        if (user == null) {
            throw new IllegalArgumentException("Пользователь с id " + telegramId + " не найден");
        }
        else {
            log.debug("Найден пользователь с id {}", telegramId);
            return user;
        }
    }

    /**
     * Проверяет существование пользователя по указанному telegramId
     *
     * @param telegramId ID пользователя в telegram
     * @return true, если пользователь существует, иначе - false
     */
    public boolean checkUserExistsByTelegramId(long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }
}
