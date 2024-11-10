package ru.naumen.repository;

import ru.naumen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий с пользователями
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Проверяет, существует ли пользователь по telegramId
     * @param telegramId Id пользователя в телеграм
     */
    boolean existsByTelegramId(long telegramId);

    /**
     * Находит пользователя с указанным telegramId
     * @param telegramId Id пользователя в телеграм
     */
    User findByTelegramId(long telegramId);
}
