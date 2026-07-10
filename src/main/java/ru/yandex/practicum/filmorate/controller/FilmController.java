package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate DATE_FIRST_MOVIE = LocalDate.of(1895, 12, 28);
    public static final int MAX_LENGTH_DESCRIPTION = 200;

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(FilmService filmService, FilmStorage filmStorage) {
        this.filmService = filmService;
        this.filmStorage = filmStorage;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов");
        Collection<Film> films = filmStorage.findAll();
        log.debug("Получен список фильмов {}", films);
        return films;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Запрос на создание фильма {}", film);
        if (film.getName() == null) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getDuration() == null) {
            throw new ValidationException("Продолжительность должна быть указана");
        }
        validate(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм создан: id={}, name={}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Запрос на изменение фильма {}", newFilm);
        validate(newFilm);
        Film updatedFilm = filmStorage.update(newFilm);
        log.info("Фильм {} обновлен", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на добавление лайка к фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
        log.info("Лайк добавлен");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка к фильму {} от пользователя {}", id, userId);
        filmService.deleteLike(id, userId);
        log.info("Лайк удален");

    }

    @GetMapping("/popular")
    public List<Film> getTopFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос на получение списка {} лучших фильмов", count);
        return filmService.getTopFilms(count);
    }

    private void validate(Film film) {
        if (film.getName() != null && !hasText(film.getName())) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(DATE_FIRST_MOVIE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
