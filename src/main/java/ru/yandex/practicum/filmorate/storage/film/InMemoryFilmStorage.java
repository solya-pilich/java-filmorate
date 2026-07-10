package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
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
    public void clear() {
        films.clear();
        log.debug("Произошла очистка хранилища фильмов");
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
