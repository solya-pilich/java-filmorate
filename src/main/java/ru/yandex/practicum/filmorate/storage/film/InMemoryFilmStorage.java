package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }

        String newName = newFilm.getName();
        String newDescription = newFilm.getDescription();
        LocalDate newReleaseDate = newFilm.getReleaseDate();
        Integer newDuration = newFilm.getDuration();

        if (newName != null) {
            oldFilm.setName(newName);
        }
        if (newDescription != null) {
            oldFilm.setDescription(newDescription);
        }
        if (newReleaseDate != null) {
            oldFilm.setReleaseDate(newReleaseDate);
        }
        if (newDuration != null) {
            oldFilm.setDuration(newDuration);
        }

        log.debug("Фильм с id {} обновлён в хранилище", oldFilm.getId());
        return oldFilm;
    }

    @Override
    public Film getById(Long filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        log.debug("Получен фильм по ID {}", filmId);
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        if (film.getWhoLikes().contains(user.getId())) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", user.getId(), film.getId());
            return;
        }
        film.getWhoLikes().add(user.getId());
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = getById(filmId);

        if (!film.getWhoLikes().contains(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            return;
        }

        film.getWhoLikes().remove(userId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getWhoLikes().size(), f1.getWhoLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Получен ID {}", newId);
        return newId;
    }

}
