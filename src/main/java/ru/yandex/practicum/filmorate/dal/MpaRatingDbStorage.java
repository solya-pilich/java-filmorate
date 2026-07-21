package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MpaRatingDbStorage extends BaseRepository<MpaRating> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_rating ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_rating WHERE id = ?";

    public MpaRatingDbStorage(JdbcTemplate jdbc, MpaRatingRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<MpaRating> findAll() {
        return new ArrayList<>(findMany(FIND_ALL_QUERY));
    }

    public Optional<MpaRating> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}