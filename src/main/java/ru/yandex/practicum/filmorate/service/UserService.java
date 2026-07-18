package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user.getFriendsIds().contains(friendId)) {
            log.warn("Пользователь {} уже является другом пользователя {}", friendId, userId);
            return;
        }

        user.getFriendsIds().add(friendId);
        friend.getFriendsIds().add(userId);

        user.getFriendshipStatusMap().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriendshipStatusMap().put(userId, FriendshipStatus.CONFIRMED);

        log.debug("Пользователь {} и {} стали друзьями", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (!user.getFriendsIds().contains(friendId)) {
            log.warn("Пользователь {} не является другом {}", friendId, userId);
            return;
        }

        user.getFriendsIds().remove(friendId);
        friend.getFriendsIds().remove(userId);

        user.getFriendshipStatusMap().remove(friendId);
        user.getFriendshipStatusMap().remove(userId);

        log.debug("Пользователь {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        return userStorage.getAllFriends(userId);
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> commonsIds = userStorage.getCommonFriends(userId1, userId2);

        if (commonsIds.isEmpty()) {
            log.info("У пользователей {} и {} нет общих друзей", userId1, userId2);
        } else {
            log.debug("Найдено {} общих друзей для пользователей {} и {}", commonsIds.size(), userId1, userId2);
        }

        return commonsIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validationEmptyFields(user);
        validateFormat(user);
        return userStorage.create(user);
    }

    public User update(@RequestBody User newUser) {
        validateFormat(newUser);
        return userStorage.update(newUser);
    }

    private void validationEmptyFields(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (user.getLogin() == null) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }
    }

    private void validateFormat(User user) {
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
