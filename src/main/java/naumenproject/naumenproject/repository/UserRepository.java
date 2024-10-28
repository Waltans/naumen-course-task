package naumenproject.naumenproject.repository;

import naumenproject.naumenproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByTelegramId(long telegramId);
    User findByTelegramId(long telegramId);
}
