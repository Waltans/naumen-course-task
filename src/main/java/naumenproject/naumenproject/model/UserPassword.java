package naumenproject.naumenproject.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Класс пароля
 */
@Entity
@Table(name = "tbl_passwords")
public class UserPassword {

    /**
     * Уникальный идентификатор пароля, UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    /**
     * Описание пароля
     */
    @Column(name = "description", nullable = true, unique = false)
    private String description;

    /**
     * Пароль в зашифрованном виде
     */
    @Column(name = "password", nullable = false, unique = false)
    private String password;

    /**
     * Пользователь, которому принадлежит пароль
     */
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private User user;

    public UserPassword(String uuid, String description, String password, User user) {
        this.uuid = uuid;
        this.description = description;
        this.password = password;
        this.user = user;
    }

    public UserPassword() {

    }

    public String getUuid() {
        return uuid;
    }

    public String getDescription() {
        return description;
    }

    public String getPassword() {
        return password;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPassword userPassword1 = (UserPassword) o;
        return Objects.equals(uuid, userPassword1.uuid) && Objects.equals(description, userPassword1.description) && Objects.equals(password, userPassword1.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, description, password);
    }

    /**
     * Билдер для UserPassword
     */
    public static class Builder {
        private String uuid;
        private String description;
        private String password;
        private User user;

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public UserPassword build() {
            return new UserPassword(uuid, description, password, user);
        }
    }
}