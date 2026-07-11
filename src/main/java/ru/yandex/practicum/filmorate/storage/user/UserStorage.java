package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    User getById(Long userId);

    List<User> getAllFriends(Long userId);

    Set<Long> getFriendsIds(Long userId);

    void clear();
}
