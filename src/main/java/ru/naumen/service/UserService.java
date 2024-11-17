package ru.naumen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.repository.UserRepository;

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
     * Создаёт пользователя и сохраняет в БД, если его ещё нет
     *
     * @param id ID пользователя
     * @param name       имя пользоваетля
     */
    @Transactional
    public void createUserIfUserNotExists(long id, String name) {
        if (userRepository.existsById(id)) {
            log.trace("Пользователь с Id {} уже существует", id);
        } else {
            User user = new User(name, id);

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
}
