package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
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

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<UserDto> findAll() {
        return userStorage.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto create(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        validationEmptyFields(user);
        validateFormat(user);
        User userSaved = userStorage.create(user);
        return UserMapper.mapToUserDto(userSaved);
    }

    public UserDto update(UpdateUserRequest request) {
        User user = userStorage.getById(request.getId());
        User updatedUser = UserMapper.updateUserFields(user, request);
        validateFormat(updatedUser);
        User savedUser = userStorage.update(updatedUser);
        return UserMapper.mapToUserDto(savedUser);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.addFriend(userId, friendId);
        log.debug("Пользователь {} и {} стали друзьями", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.debug("Пользователь {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<UserDto> getAllFriends(Long userId) {
        userStorage.getById(userId);
        return userStorage.getAllFriends(userId)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
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
