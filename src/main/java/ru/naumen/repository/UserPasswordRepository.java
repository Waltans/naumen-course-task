package ru.naumen.repository;

import ru.naumen.model.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий с паролями пользователей
 */
public interface UserPasswordRepository extends JpaRepository<UserPassword, Long> {
    /**
     * Находит список паролей для пользователя с указанным id
     * @param userTelegramId Id пользователя в телеграм
     */
    List<UserPassword> findByUserTelegramId(long userTelegramId);

    /**
     * Проверяет, существует ли пароль с указанным uuid
     * @param uuid uuid пароля
     */
    boolean existsByUuid(String uuid);

    /**
     * Удаляет пароль с указанным uuid
     * @param uuid uuid пароля
     */
    void deleteByUuid(String uuid);

    /**
     * Находит запись пароля с указанным uuid
     * @param uuid uuid пароля
     */
    UserPassword findByUuid(String uuid);
}
