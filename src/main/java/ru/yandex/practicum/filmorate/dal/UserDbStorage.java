package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE users " +
            "SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id, friendshipStatus) " +
            "VALUES (?, ?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_ALL_FRIEND_QUERY = "SELECT u.* FROM friendship f " +
            "JOIN users u ON u.id = f.friend_id " +
            "WHERE f.user_id = ?";
    private static final String FIND_COMMON_FRIEND_QUERY = "SELECT f1.friend_id FROM friendship f1 " +
            "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User getById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public User create(User user) {
        long id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {
        update(UPDATE_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                java.sql.Date.valueOf(newUser.getBirthday()),
                newUser.getId());
        return newUser;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(INSERT_FRIEND_QUERY, userId, friendId, FriendshipStatus.CONFIRMED.name());

    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        int rows = jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getAllFriends(Long userId) {
        return jdbc.query(FIND_ALL_FRIEND_QUERY, mapper, userId);
    }

    @Override
    public Set<Long> getCommonFriends(Long user1Id, Long user2Id) {
        List<Long> ids = jdbc.queryForList(FIND_COMMON_FRIEND_QUERY, Long.class, user1Id, user2Id);
        return new HashSet<>(ids);
    }
}
