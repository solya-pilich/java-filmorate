package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;


    @GetMapping
    public Collection<FilmDto> findAll() {
        log.info("Запрос на получение всех фильмов");
        Collection<FilmDto> films = filmService.findAll();
        log.debug("Получен список фильмов {}", films);
        return films;
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с id {}", id);
        FilmDto film = filmService.getById(id);
        log.debug("Получен фильм {}", film);
        return film;
    }


    @PostMapping
    public FilmDto create(@RequestBody NewFilmRequest request) {
        log.info("Received request: mpa = {}", request.getMpaRating()); //
        log.info("Received request: genres = {}", request.getGenres());
        log.info("Запрос на создание фильма {}", request);
        FilmDto createdFilm = filmService.create(request);
        log.info("Фильм создан: id={}, name={}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    @PutMapping
    public FilmDto update(@RequestBody UpdateFilmRequest request) {
        log.info("Запрос на изменение фильма {}", request);
        FilmDto updatedFilm = filmService.update(request);
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
    public List<FilmDto> getTopFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос на получение списка {} лучших фильмов", count);
        return filmService.getTopFilms(count);
    }

}
