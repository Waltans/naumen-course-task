package ru.naumen.repository;

import ru.naumen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий с пользователями
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Проверяет, существует ли пользователь по Id
     * @param id Id пользователя
     */
    boolean existsById(long id);

    /**
     * Находит пользователя с указанным Id
     * @param id Id пользователя
     */
    User findById(long id);
}
