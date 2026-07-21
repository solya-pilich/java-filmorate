package ru.yandex.practicum.filmorate;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class, MpaRatingRowMapper.class,
        UserDbStorage.class, UserRowMapper.class})
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
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

    private Film createTestFilm(String name, String description, int duration, int ratingId, Set<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(duration);
        film.setRating(new MpaRating(ratingId, null));
        film.setGenres(genres != null ? genres : new HashSet<>());
        return filmStorage.create(film);
    }

    @Test
    void findAll_shouldReturnAllFilms() {
        createTestFilm("Film1", "Desc1", 100, 1, null);
        createTestFilm("Film2", "Desc2", 120, 2, null);

        List<Film> films = (List<Film>) filmStorage.findAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film1", "Film2");
    }

    @Test
    void getById_shouldReturnFilm_whenExists() {
        Film created = createTestFilm("Test Film", "Test Desc", 90, 1, null);
        Long id = created.getId();

        Film found = filmStorage.getById(id);

        assertThat(found).isEqualTo(created);
        assertThat(found.getRating()).isNotNull();
        assertThat(found.getRating().getId()).isEqualTo(1);
        assertThat(found.getGenres()).isEmpty();
    }

    @Test
    void getById_shouldThrowNotFoundException_whenNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmStorage.getById(100L));
        assertTrue(exception.getMessage().contains("100"));
    }

    @Test
    void create_shouldSaveFilmWithGenresAndRating() {
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, null));
        genres.add(new Genre(2, null));

        Film film = new Film();
        film.setName("Film with genres");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2022, 1, 1));
        film.setDuration(150);
        film.setRating(new MpaRating(3, null));
        film.setGenres(genres);

        Film saved = filmStorage.create(film);

        assertThat(saved.getId()).isPositive();

        Film fromDb = filmStorage.getById(saved.getId());
        assertThat(fromDb.getGenres()).hasSize(2);
        assertThat(fromDb.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);

        assertThat(fromDb.getRating()).isNotNull();
        assertThat(fromDb.getRating().getId()).isEqualTo(3);
    }

    @Test
    void update_shouldUpdateFilmFields() {
        Film created = createTestFilm("Old Name", "Old Desc", 100, 1, null);
        Long id = created.getId();

        Film toUpdate = filmStorage.getById(id);
        toUpdate.setName("New Name");
        toUpdate.setDescription("New Desc");
        toUpdate.setDuration(200);
        toUpdate.setReleaseDate(LocalDate.of(2023, 2, 2));
        toUpdate.setRating(new MpaRating(4, null));
        filmStorage.update(toUpdate);

        Film updated = filmStorage.getById(id);
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        assertThat(updated.getDuration()).isEqualTo(200);
        assertThat(updated.getReleaseDate()).isEqualTo(LocalDate.of(2023, 2, 2));
        assertThat(updated.getRating().getId()).isEqualTo(4);
    }

    @Test
    void addLike_shouldInsertLike() {
        User user = createTestUser("user@example.com", "user", "User");
        Film film = createTestFilm("Film", "Desc", 100, 1, null);

        filmStorage.addLike(film, user);

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, film.getId(), user.getId()
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteLike_shouldRemoveLike() {
        User user = createTestUser("user@example.com", "user", "User");
        Film film = createTestFilm("Film", "Desc", 100, 1, null);

        filmStorage.addLike(film, user);
        filmStorage.deleteLike(film.getId(), user.getId());

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, film.getId(), user.getId()
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void deleteLike_shouldThrowNotFoundException_whenLikeNotFound() {
        User user = createTestUser("user@example.com", "user", "User");
        Film film = createTestFilm("Film", "Desc", 100, 1, null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmStorage.deleteLike(film.getId(), user.getId()));
        assertTrue(exception.getMessage().contains("Лайк не найден"));
    }

    @Test
    void getTopFilms_shouldReturnFilmsSortedByLikes() {
        User user1 = createTestUser("u1@example.com", "u1", "User1");
        User user2 = createTestUser("u2@example.com", "u2", "User2");
        User user3 = createTestUser("u3@example.com", "u3", "User3");

        Film film1 = createTestFilm("Film A", "Desc", 100, 1, null);
        Film film2 = createTestFilm("Film B", "Desc", 110, 2, null);
        Film film3 = createTestFilm("Film C", "Desc", 120, 3, null);

        // лайки: film1 – 2 лайка, film2 – 1 лайк, film3 – 0 лайков
        filmStorage.addLike(film1, user1);
        filmStorage.addLike(film1, user2);
        filmStorage.addLike(film2, user3);

        List<Film> top = filmStorage.getTopFilms(3);

        assertThat(top).hasSize(3);

        assertThat(top.get(0).getId()).isEqualTo(film1.getId());
        assertThat(top.get(1).getId()).isEqualTo(film2.getId());
        assertThat(top.get(2).getId()).isEqualTo(film3.getId());
    }
}

