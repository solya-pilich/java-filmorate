package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Запрос на получение всех пользователей");
        Collection<UserDto> users = userService.findAll();
        log.debug("Получен список пользователей: {}", users);
        return users;
    }

    @PostMapping
    public UserDto create(@RequestBody NewUserRequest request) {
        log.info("Запрос на создание нового пользователя: {}", request);
        UserDto createdUser = userService.create(request);
        log.info("Пользователь создан: id={}, email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    @PutMapping
    public UserDto update(@RequestBody UpdateUserRequest request) {
        log.info("Запрос на изменение пользователя {}", request);
        UserDto oldUser = userService.update(request);
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
    public List<UserDto> getAllFriends(@PathVariable Long id) {
        log.info("Запрос на получения списка друзей пользователя {}", id);
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос на получения списка общих друзей пользователя {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
