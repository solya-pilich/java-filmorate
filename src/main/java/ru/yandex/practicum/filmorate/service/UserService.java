package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        log.debug("Пользователь {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        User user = userStorage.getById(userId);
        return user.getFriendsIds().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = userStorage.getById(userId1);
        User user2 = userStorage.getById(userId2);

        Set<Long> commonsIds = user1.getFriendsIds().stream()
                .filter(user2.getFriendsIds()::contains)
                .collect(Collectors.toSet());

        if (commonsIds.isEmpty()) {
            log.info("У пользователей {} и {} нет общих друзей", userId1, userId2);
        } else {
            log.debug("Найдено {} общих друзей для пользователей {} и {}", commonsIds.size(), userId1, userId2);
        }

        return commonsIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());

    }
}
