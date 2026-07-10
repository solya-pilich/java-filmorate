package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение всех пользователей");
        Collection<User> users = userStorage.findAll();
        log.debug("Получен список пользователей: {}", users);
        return users;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);
        if (user.getEmail() == null) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (user.getLogin() == null) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }
        validate(user);
        User createdUser = userStorage.create(user);
        log.info("Пользователь создан: id={}, email={}", user.getId(), user.getEmail());
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Запрос на изменение пользователя {}", newUser);
        validate(newUser);
        User oldUser = userStorage.update(newUser);
        log.info("Пользователь {} обновлен", oldUser);
        return oldUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на добавление пользователя {} в друзья к {}", friendId, id);
        userService.addFriend(id, friendId);
        log.info("Пользователь {} и {} теперь друзья", friendId, id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на удаление пользователя {} из друзей у {}", friendId, id);
        userService.deleteFriend(id, friendId);
        log.info("Пользователь {} и {} больше не друзья", friendId, id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Long id) {
        log.info("Запрос на получения списка друзей пользователя {}", id);
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос на получения списка общих друзей пользователя {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    private void validate(User user) {
        if (user.getEmail() != null && (!StringUtils.hasText(user.getEmail()) || !user.getEmail().contains("@"))) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() != null && (!StringUtils.hasText(user.getLogin()) || user.getLogin().contains(" "))) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения должна быть указана и не может быть в будущем");
        }
    }
}
