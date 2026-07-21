package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreMapper;
    private final MpaRatingRowMapper mpaRatingRowMapper;

    public FilmRowMapper(JdbcTemplate jdbc, GenreRowMapper genreMapper, MpaRatingRowMapper mpaRatingRowMapper) {
        this.jdbc = jdbc;
        this.genreMapper = genreMapper;
        this.mpaRatingRowMapper = mpaRatingRowMapper;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Integer ratingId = rs.getObject("rating_id", Integer.class);
        if (ratingId != null) {
            try {
                String ratingSql = "SELECT id, name FROM mpa_rating WHERE id = ?";
                MpaRating rating = jdbc.queryForObject(ratingSql, mpaRatingRowMapper, ratingId);
                film.setRating(rating);  // предполагаем, что метод называется setMpaRating
            } catch (Exception e) {
                film.setRating(null);
            }
        }

        String genreSql = "SELECT g.id, g.name FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ?";
        List<Genre> genres = jdbc.query(genreSql, genreMapper, film.getId());
        genres.sort(Comparator.comparingInt(Genre::getId));
        film.setGenres(new LinkedHashSet<>(genres));

        return film;
    }
}
