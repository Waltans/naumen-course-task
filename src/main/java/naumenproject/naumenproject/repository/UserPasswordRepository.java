package naumenproject.naumenproject.repository;

import naumenproject.naumenproject.model.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPasswordRepository extends JpaRepository<UserPassword, Long> {
    List<UserPassword> findByUserTelegramId(long userTelegramId);

    boolean existsByUuid(String uuid);

    void deleteByUuid(String uuid);

    UserPassword findByUuid(String uuid);
}
