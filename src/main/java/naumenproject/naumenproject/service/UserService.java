package naumenproject.naumenproject.service;

import naumenproject.naumenproject.model.User;
import naumenproject.naumenproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void createUser(long telegramId, String name) {
        User user = new User();
        user.setUsername(name);
        user.setTelegramId(telegramId);

        userRepository.save(user);
        log.info("Создан пользователь с telegram id {}", telegramId);
    }

    /**
     * Возвращает пользователя по его Telegram ID.
     *
     * @param telegramId telegram ID пользователя
     */
    @Transactional(readOnly = true)
    public User getUserByTelegramId(long telegramId) {
        User user = userRepository.findByTelegramId(telegramId);

        if (user == null) {
            throw new IllegalArgumentException(String.format("Пользователь с id %s не найден", telegramId));
        } else {
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
    @Transactional(readOnly = true)
    public boolean checkUserExistsByTelegramId(long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }
}
