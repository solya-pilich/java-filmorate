package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@Import({MpaRatingDbStorage.class, MpaRatingRowMapper.class})
class MpaRatingDbStorageTest {

    private final MpaRatingDbStorage mpaRatingStorage;

    @Test
    void findAll_shouldReturnAllMpaRatings() {
        List<MpaRating> ratings = mpaRatingStorage.findAll();

        assertThat(ratings).isNotEmpty();
        assertThat(ratings).hasSize(5);
        assertThat(ratings).extracting(MpaRating::getId)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(ratings).extracting(MpaRating::getName)
                .containsExactly(
                        "G",
                        "PG",
                        "PG-13",
                        "R",
                        "NC-17"
                );
    }

    @Test
    void findById_shouldReturnMpaRating_whenExists() {
        Optional<MpaRating> rating = mpaRatingStorage.findById(3);

        assertThat(rating).isPresent();
        assertThat(rating.get().getId()).isEqualTo(3);
        assertThat(rating.get().getName()).isEqualTo("PG-13");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<MpaRating> rating = mpaRatingStorage.findById(999);
        assertThat(rating).isEmpty();
    }
}
