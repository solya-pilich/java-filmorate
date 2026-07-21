package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;


import java.util.Collection;
import java.util.List;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE films " +
            "SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ? " +
            "WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, releaseDate, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_TOP_QUERY = "SELECT f.*, COUNT(fl.user_id) AS likes_count " +
            "FROM films f " +
            "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
            "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.rating_id " +
            "ORDER BY likes_count DESC " +
            "LIMIT ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film getById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(genreSql, id, genre.getId());
            }
        }

        return getById(id);
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null,
                film.getId());
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        jdbc.update(INSERT_LIKE_QUERY, film.getId(), user.getId());
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        int rows = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
        if (rows == 0) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return jdbc.query(GET_TOP_QUERY, mapper, count);
    }
}
