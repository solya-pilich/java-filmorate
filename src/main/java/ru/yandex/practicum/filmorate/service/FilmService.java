package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate DATE_FIRST_MOVIE = LocalDate.of(1895, 12, 28);
    public static final int MAX_LENGTH_DESCRIPTION = 200;

    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreDbStorage genreDbStorage, MpaRatingDbStorage mpaRatingDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDbStorage = genreDbStorage;
        this.mpaRatingDbStorage = mpaRatingDbStorage;
    }

    public Collection<FilmDto> findAll() {
        return filmStorage.findAll()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getById(Long id) {
        Film film = filmStorage.getById(id);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film film = filmStorage.getById(request.getId());
        Film updatedFilm = FilmMapper.updateFilmFields(film, request);
        validateFormat(updatedFilm);
        Film savedFilm = filmStorage.update(updatedFilm);
        return FilmMapper.mapToFilmDto(savedFilm);
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        validateGenres(request.getGenres());
        validateMpa(request.getMpaRating());
        validationEmptyFields(film);
        validateFormat(film);
        Film saved = filmStorage.create(film);
        return FilmMapper.mapToFilmDto(saved);
    }

    public void addLike(Long filmId, Long userId) {
        User user = userStorage.getById(userId);
        Film film = filmStorage.getById(filmId);
        filmStorage.addLike(film, user);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
        filmStorage.deleteLike(filmId, userId);
        log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<FilmDto> getTopFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным");
        }
        return filmStorage.getTopFilms(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    private void validationEmptyFields(Film film) {
        if (film.getName() == null) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getDuration() == null) {
            throw new ValidationException("Продолжительность должна быть указана");
        }
    }

    private void validateFormat(Film film) {
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

    private void validateGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;
        for (Genre genre : genres) {
            if (genreDbStorage.findById(genre.getId()).isEmpty()) {
                throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
            }
        }
    }

    private void validateMpa(MpaRating mpa) {
        if (mpa == null) return;
        if (mpaRatingDbStorage.findById(mpa.getId()).isEmpty()) {
            throw new NotFoundException("MPA-рейтинг с id " + mpa.getId() + " не найден");
        }
    }
}
