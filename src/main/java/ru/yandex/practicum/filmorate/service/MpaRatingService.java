package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
public class MpaRatingService {
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public MpaRatingService(MpaRatingDbStorage mpaRatingDbStorage) {
        this.mpaRatingDbStorage = mpaRatingDbStorage;
    }

    public List<MpaRating> findAll() {
        return mpaRatingDbStorage.findAll();
    }

    public MpaRating findById(int id) {
        return mpaRatingDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + id + " не найден"));
    }
}
