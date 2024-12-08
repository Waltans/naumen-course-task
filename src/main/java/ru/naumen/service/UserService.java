package ru.naumen.service;

import ch.qos.logback.core.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.repository.UserRepository;

/**
 * Класс для работы с пользователями
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EncodeService encodeService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, EncodeService encodeService) {
        this.userRepository = userRepository;
        this.encodeService = encodeService;
    }

    /**
     * Создаёт пользователя и сохраняет в БД, если его ещё нет
     *
     * @param id ID пользователя
     */
    public void createUserIfUserNotExists(long id) {
        if (userRepository.existsById(id)) {
            log.trace("Пользователь с Id {} уже существует", id);
        } else {
            User user = new User(id);

            userRepository.save(user);
            log.info("Создан пользователь с id {}", id);
        }
    }

    /**
     * Возвращает пользователя по его ID.
     *
     * @param id ID пользователя
     */
    public User getUserById(long id) throws UserNotFoundException {
        User user = userRepository.findById(id);

        if (user == null) {
            throw new UserNotFoundException(String.format("Пользователь с id %s не найден", id));
        } else {
            log.debug("Найден пользователь с id {}", id);
            return user;
        }
    }

    /**
     * Добавление кодового слова для пользователя
     *
     * @param userId   - ID пользователя
     * @param codeWord - кодовое слово
     * @throws UserNotFoundException - ошибка, в случае, если не удалось засеттить кодовое слово пользователю
     */
    public void addCodeWordForUser(long userId, String codeWord) throws UserNotFoundException, UserCodePhraseException {
        User user = getUserById(userId);
        try {
            user.setCodePhrase(encodeService.encryptData(codeWord));
            userRepository.save(user);
        } catch (UserCodePhraseException e) {

            throw new UserCodePhraseException("Невозможно установить кодовое слово для пользователя");
        }
    }

    /**
     * Получаем пользователя и проверяем, есть ли у него кодовое слово
     * @param userId - ID пользователя
     * @return - true, если есть кодовое слово, false - если его нет
     * @throws UserNotFoundException - ошибка, если пользователь не найден
     */
    public boolean isExistCodeWord(Long userId) throws UserNotFoundException {
        User user = getUserById(userId);

        return user.getCodePhrase() != null;
    }
}
