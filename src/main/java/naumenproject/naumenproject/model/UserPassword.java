package naumenproject.naumenproject.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Класс пароля
 */
@Entity
@Table(name = "tbl_passwords")
public class UserPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Column(name = "description", nullable = true, unique = false)
    private String description;

    @Column(name = "password", nullable = false, unique = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
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
}
