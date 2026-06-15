package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate DATE_FIRST_MOVIE = LocalDate.of(1895, 12, 28);
    public static final int MAX_LENGTH_DESCRIPTION = 200;

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов");
        log.debug("Получен список фильмов {}", films.values());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Запрос на создание фильма {}", film);
        try {
            validate(film);
            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Фильм создан: id={}, name={}", film.getId(), film.getName());
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании фильма: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Запрос на изменение фильма {}", newFilm);
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Фильм с id {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }

        String newName = newFilm.getName();
        String newDescription = newFilm.getDescription();
        LocalDate newReleaseDate = newFilm.getReleaseDate();
        Integer newDuration = newFilm.getDuration();

        if (newName != null && !newName.isBlank()) {
            oldFilm.setName(newName);
            log.debug("Обновление названия фильма на {}", newName);
        }

        if (newDescription != null) {
            if (newDescription.length() > MAX_LENGTH_DESCRIPTION) {
                log.warn("Ошибка валидации, превышена максимальная длина описания");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            oldFilm.setDescription(newDescription);
            log.debug("Обновление описания фильма на {}", newDescription);
        }

        if (newReleaseDate != null) {
            if (newReleaseDate.isBefore(DATE_FIRST_MOVIE)) {
                log.warn("Ошибка валидации, указана дата релиза до 28.12.1895");
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
            oldFilm.setReleaseDate(newReleaseDate);
            log.debug("Обновление даты релиза фильма на {}", newReleaseDate);
        }

        if (newDuration != null) {
            if (newDuration <= 0) {
                log.warn("Ошибка валидации, указана продолжительность фильма меньше 1 минуты");
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
            oldFilm.setDuration(newDuration);
            log.debug("Обновление продолжительности фильма на {}", newDuration);
        }
        log.info("Фильм {} обновлен", oldFilm);
        return oldFilm;
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый id - {}", newId);
        return newId;
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(DATE_FIRST_MOVIE)) {
            throw new ValidationException("Дата релиза должна быть указана и не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
