package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    public User update(@RequestBody User newUser) {
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }

        String newEmail = newUser.getEmail();
        String newLogin = newUser.getLogin();
        String newName = newUser.getName();
        LocalDate newBirthday = newUser.getBirthday();

        if (newEmail != null) {
            oldUser.setEmail(newEmail);
        }
        if (newLogin != null) {
            oldUser.setLogin(newLogin);
        }
        if (newName != null) {
            if (newName.isBlank()) {
                oldUser.setName(oldUser.getLogin());
            } else {
                oldUser.setName(newName);
            }
        }
        if (newBirthday != null) {
            oldUser.setBirthday(newBirthday);
        }

        log.debug("Пользователь с id {} обновлён в хранилище", oldUser.getId());
        return oldUser;
    }

    @Override
    public User getById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        log.debug("Получен пользователь по ID {}", userId);
        return user;
    }

    @Override
    public List<User> getAllFriends(Long userId) {
        User user = getById(userId);
        return user.getFriendsIds().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Long> getFriendsIds(Long userId) {
        User user = getById(userId);
        return new HashSet<>(user.getFriendsIds());
    }

    @Override
    public void clear() {
        users.clear();
        log.debug("Произошла очистка хранилища пользователей");
    }

    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Получен ID {}", newId);
        return newId;
    }
}
