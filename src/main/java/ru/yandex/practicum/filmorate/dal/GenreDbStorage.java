package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage extends BaseRepository<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";

    private final JdbcTemplate jdbc;

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    public List<Genre> findAll() {
        return new ArrayList<>(findMany(FIND_ALL_QUERY));
    }

    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public boolean existsAllByIds(Set<Integer> ids) {
        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String sql = "SELECT COUNT(*) FROM genre WHERE id IN (" + placeholders + ")";

        Integer count = jdbc.queryForObject(sql, Integer.class, ids.toArray());
        return count != null && count == ids.size();
    }
}