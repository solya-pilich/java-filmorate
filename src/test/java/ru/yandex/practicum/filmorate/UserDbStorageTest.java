package ru.yandex.practicum.filmorate;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userStorage.create(user);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        createTestUser("user1@example.com", "user1", "User One");
        createTestUser("user2@example.com", "user2", "User Two");

        List<User> users = (List<User>) userStorage.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void getById_shouldReturnUser_whenExists() {
        User created = createTestUser("test@example.com", "test", "Test User");
        Long id = created.getId();

        User found = userStorage.getById(id);

        assertThat(found).isEqualTo(created);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userStorage.getById(100L));
        assertTrue(exception.getMessage().contains("100"));
    }

    @Test
    void create_saveUserAndGenerateId() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User saved = userStorage.create(user);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        User fromDb = userStorage.getById(saved.getId());
        assertThat(fromDb).isEqualTo(saved);
    }

    @Test
    void update_shouldUpdateUser() {
        User userCreated = createTestUser("user1@example.com", "user1", "User One");
        User user = userStorage.getById(userCreated.getId());
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(2000, 12, 12));

        userStorage.update(user);

        User updated = userStorage.getById(user.getId());
        assertThat(updated.getEmail()).isEqualTo("user@example.com");
        assertThat(updated.getLogin()).isEqualTo("userlogin");
        assertThat(updated.getName()).isEqualTo("User Name");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(2000, 12, 12));
    }

    @Test
    void addFriend_shouldInsertFriendshipAndUpdateFriendLists() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        long id1 = user1.getId();
        long id2 = user2.getId();

        userStorage.addFriend(id1, 2L);

        List<User> friends1 = userStorage.getAllFriends(id1);
        assertThat(friends1).hasSize(1);
        assertThat(friends1.getFirst().getId()).isEqualTo(id2);

        List<User> friends2 = userStorage.getAllFriends(id2);
        assertThat(friends2).isEmpty();

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = 1 AND friend_id = 2",
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteFriend_shouldRemoveFriendship() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        long id1 = user1.getId();
        long id2 = user2.getId();

        userStorage.addFriend(id1, id2);
        userStorage.deleteFriend(id1, id2);

        List<User> friends = userStorage.getAllFriends(id1);
        assertThat(friends).isEmpty();

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = 1 AND friend_id = 2",
                Integer.class
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void deleteFriend_shouldNotThrowException_whenFriendshipDoesNotExist() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        long id1 = user1.getId();
        long id2 = user2.getId();

        userStorage.deleteFriend(id1, id2);

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = 1 AND friend_id = 2",
                Integer.class
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void getCommonFriends_shouldReturnIntersection() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        User user3 =  createTestUser("user3@example.com", "user3", "User Tree");
        User user4 = createTestUser("user4@example.com", "user4", "User Four");
        long id1 = user1.getId();
        long id2 = user2.getId();
        long id3 = user3.getId();
        long id4 = user4.getId();

        userStorage.addFriend(id1, id2);
        userStorage.addFriend(id1, id3);

        userStorage.addFriend(id2, id3);
        userStorage.addFriend(id2, id4);

        Set<Long> common = userStorage.getCommonFriends(id1, id2);

        assertThat(common).hasSize(1);
        assertThat(common).containsExactly(id3);
    }

    @Test
    void getCommonFriends_shouldReturnEmptySet_whenNoCommonFriends() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        User user3 =  createTestUser("user3@example.com", "user3", "User Tree");
        User user4 = createTestUser("user4@example.com", "user4", "User Four");
        long id1 = user1.getId();
        long id2 = user2.getId();
        long id3 = user3.getId();
        long id4 = user4.getId();

        userStorage.addFriend(id1, id3);
        userStorage.addFriend(id2, id4);

        Set<Long> common = userStorage.getCommonFriends(id1, id2);

        assertThat(common).isEmpty();
    }
}
