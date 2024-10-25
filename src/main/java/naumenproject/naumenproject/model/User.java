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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Column(name = "username", nullable = false)
    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPassword> userPasswords;

    public User(String uuid, String username, List<UserPassword> userPasswords) {
        this.uuid = uuid;
        this.username = username;
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
}