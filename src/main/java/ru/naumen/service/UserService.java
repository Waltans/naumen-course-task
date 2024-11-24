package ru.naumen.service;

import ch.qos.logback.core.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.naumen.exception.UserCodePhraseException;
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
    @Transactional
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
    @Transactional(readOnly = true)
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
     * @throws UserNotFoundException - ошибка, в случае, если кодовое слово уже задано
     */
    public void addCodeWordForUser(long userId, String codeWord) throws UserNotFoundException, UserCodePhraseException {
        User user = getUserById(userId);

        if (!StringUtil.isNullOrEmpty(user.getCodePhrase())) {
            throw new UserCodePhraseException(String.format("У пользователя с id %s уже задано кодовое слово", userId));
        }
        user.setCodePhrase(encodeService.encryptData(codeWord));
        userRepository.save(user);
    }

    public boolean isExistCodeWord(Long userId) throws UserNotFoundException {
        User user = getUserById(userId);

        return user.getCodePhrase() != null;
    }
}