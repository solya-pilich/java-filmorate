package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;

@SpringBootTest
class FilmorateApplicationTests {

    public static final int NON_EXISTENT_ID = 105;
    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void create() {
        filmController = new FilmController();
        userController = new UserController();
    }

    Film createValidFilm() {
        return Film.builder()
                .name("Тест1")
                .description("Тестовое описание")
                .releaseDate(LocalDate.of(2020, 10, 15))
                .duration(120)
                .build();
    }

    User createValidUser() {
        return User.builder()
                .email("email@.ru")
                .login("Логин")
                .name("Имя")
                .birthday(LocalDate.of(2002, 8, 28))
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getAllFilms_ShouldSucceed() {
        Film film1 = createValidFilm();
        filmController.create(film1);

        Film film2 = createValidFilm();
        filmController.create(film2);
        Collection<Film> films = filmController.findAll();

        assertEquals(2, films.size());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
    }

    @Test
    void postValidFilm_ShouldSucceed() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);

        assertEquals(1, createdFilm.getId());
        assertEquals("Тест1", createdFilm.getName());
        assertEquals("Тестовое описание", createdFilm.getDescription());
        assertEquals(LocalDate.of(2020, 10, 15), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());
    }

    @Test
    void postFilm_WithEmptyName_ShouldThrow() {
        Film film = createValidFilm();
        film.setName("");

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void postFilm_With200Description_ShouldSucceed() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        Film createdFilm = filmController.create(film);

        assertEquals(1, createdFilm.getId());
        assertEquals(200, createdFilm.getDescription().length());
    }

    @Test
    void postFilm_With201Description_ShouldThrow() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void postFilm_WithBorderlineDate_ShouldSucceed() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film createdFilm = filmController.create(film);

        assertEquals(1, createdFilm.getId());
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate());
    }

    @Test
    void postFilm_WithTooEarlyDate_ShouldThrow() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года",
                exception.getMessage());
    }

    @Test
    void postFilm_WithEmptyDate_ShouldThrow() {
        Film film = createValidFilm();
        film.setReleaseDate(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года",
                exception.getMessage());
    }

    @Test
    void postFilm_WithDuration0_ShouldThrow() {
        Film film = createValidFilm();
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом",
                exception.getMessage());
    }

    @Test
    void postFilm_WithNegativeDuration_ShouldThrow() {
        Film film = createValidFilm();
        film.setDuration(-50);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом",
                exception.getMessage());
    }

    @Test
    void postFilm_WithEmptyDuration_ShouldThrow() {
        Film film = createValidFilm();
        film.setDuration(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом",
                exception.getMessage());
    }

    @Test
    void putValidFilm_ShouldSucceed() {
        Film oldFilm = createValidFilm();
        Film createdFilm = filmController.create(oldFilm);
        long filmId = createdFilm.getId();

        Film newFilm = Film.builder()
                .id(filmId)
                .name("Обновленное названия")
                .description("Тестовое описание")
                .releaseDate(LocalDate.of(2020, 10, 15))
                .duration(50)
                .build();

        Film updatedFilm = filmController.update(newFilm);
        assertEquals(1, filmController.findAll().size());
        assertEquals("Обновленное названия", updatedFilm.getName());
        assertEquals(50, updatedFilm.getDuration());
        assertEquals(createdFilm.getDescription(), updatedFilm.getDescription());
        assertEquals(createdFilm.getReleaseDate(), updatedFilm.getReleaseDate());
    }

    @Test
    void putFilm_WithIncorrectId_ShouldThrow() {
        Film oldFilm = createValidFilm();
        filmController.create(oldFilm);

        Film newFilm = Film.builder()
                .id(NON_EXISTENT_ID)
                .name("Обновленное названия")
                .description("Тестовое описание")
                .releaseDate(LocalDate.of(2020, 10, 15))
                .duration(120)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmController.update(newFilm));
        assertEquals("Фильм с id " + NON_EXISTENT_ID + " не найден",
                exception.getMessage());

        Collection<Film> films = filmController.findAll();
        assertEquals(1, films.size());
        assertTrue(films.contains(oldFilm));
    }

    @Test
    void getAllUsers_ShouldSucceed() {
        User user1 = createValidUser();
        userController.create(user1);

        User user2 = createValidUser();
        userController.create(user2);

        Collection<User> users = userController.findAll();

        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    void postValidUser_ShouldSucceed() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        assertEquals(1, createdUser.getId());
        assertEquals("email@.ru", createdUser.getEmail());
        assertEquals("Логин", createdUser.getLogin());
        assertEquals("Имя", createdUser.getName());
        assertEquals(LocalDate.of(2002, 8, 28), createdUser.getBirthday());
    }

    @Test
    void postUser_WithEmptyEmail_ShouldThrow() {
        User user = createValidUser();
        user.setEmail(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void postUser_WithIncorrectEmail_ShouldThrow() {
        User user = createValidUser();
        user.setEmail("email.ru");

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void postUser_WithEmptyLogin_ShouldThrow() {
        User user = createValidUser();
        user.setLogin(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithLoginWithWhitespace_ShouldThrow() {
        User user = createValidUser();
        user.setLogin("Логин логин");

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithEmptyBirthday_ShouldThrow() {
        User user = createValidUser();
        user.setBirthday(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Дата рождения должна быть указана и не может быть в будущем", exception.getMessage());
    }

    @Test
    void postUser_WithBorderlineBirthday_ShouldThrow() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        User createdUser = userController.create(user);

        assertEquals(1, createdUser.getId());
        assertEquals(LocalDate.now(), createdUser.getBirthday());
    }

    @Test
    void postUser_WithTooLateBirthday_ShouldThrow() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Дата рождения должна быть указана и не может быть в будущем", exception.getMessage());
    }

    @Test
    void putValidUser_ShouldSucceed() {
        User oldUser = createValidUser();
        User createdUser = userController.create(oldUser);
        long userId = createdUser.getId();

        User newUser = User.builder()
                .id(userId)
                .email("new.email@.ru")
                .login("Логин")
                .name("")
                .birthday(LocalDate.of(2020, 8, 28))
                .build();

        User updatedUser = userController.update(newUser);
        assertEquals(1, updatedUser.getId());
        assertEquals("new.email@.ru", updatedUser.getEmail());
        assertEquals("Логин", updatedUser.getLogin());
        assertEquals("Логин", updatedUser.getName());
        assertEquals(LocalDate.of(2020, 8, 28), updatedUser.getBirthday());
    }

    @Test
    void putUser_WithIncorrectId_ShouldThrow() {
        User oldUser = createValidUser();
        userController.create(oldUser);

        User newUser = User.builder()
                .id(NON_EXISTENT_ID)
                .email("email@.ru")
                .login("Логин")
                .name("Имя")
                .birthday(LocalDate.of(2002, 8, 28))
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userController.update(newUser));
        assertEquals("Пользователь с id " + NON_EXISTENT_ID + " не найден",
                exception.getMessage());

        Collection<User> users = userController.findAll();
        assertEquals(1, users.size());
        assertTrue(users.contains(oldUser));
    }
}
