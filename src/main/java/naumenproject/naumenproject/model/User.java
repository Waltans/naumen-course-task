package naumenproject.naumenproject.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

/**
 * Основной класс с пользователями бота
 */
@Entity
@Table(name = "tbl_users")
public class User {

    /**
     * Уникальный идентификатор пользователя, UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    /**
     * Имя пользователя
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * Идентификатор пользователя в Telegram
     */
    @Column(name = "telegram_id", nullable = false)
    private long telegramId;

    /**
     * Список паролей пользователя
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPassword> userPasswords;

    public User(String uuid, String username, long telegramId, List<UserPassword> userPasswords) {
        this.uuid = uuid;
        this.username = username;
        this.telegramId = telegramId;
        this.userPasswords = userPasswords;
    }

    public User() {

    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public List<UserPassword> getPasswords() {
        return userPasswords;
    }

    public void setPasswords(UserPassword userPassword) {
        this.userPasswords.add(userPassword);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uuid, user.uuid) && Objects.equals(username, user.username) && Objects.equals(userPasswords, user.userPasswords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username, userPasswords);
    }

    /**
     * Билдер для User
     */
    public static class Builder {
        private String uuid;
        private String username;
        private long telegramId;
        private List<UserPassword> userPasswords;

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder telegramId(long telegramId) {
            this.telegramId = telegramId;
            return this;
        }

        public Builder userPasswords(List<UserPassword> userPasswords) {
            this.userPasswords = userPasswords;
            return this;
        }

        public User build() {
            return new User(uuid, username, telegramId, userPasswords);
        }
    }
}