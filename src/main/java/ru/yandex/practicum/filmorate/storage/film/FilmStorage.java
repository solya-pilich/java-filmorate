package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Film getById(Long filmId);

    void addLike(Film film, User user);

    void deleteLike(Long filmId, Long userId);

    List<Film> getTopFilms(int count);

    void clear();
}
