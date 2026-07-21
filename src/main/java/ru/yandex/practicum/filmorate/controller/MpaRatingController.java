package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mpa")
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    public MpaRatingController(MpaRatingService mpaRatingService) {
        this.mpaRatingService = mpaRatingService;
    }

    @GetMapping
    public List<MpaRating> getAllMpa() {
        log.info("Запрос на получение всех рейтингов");
        return mpaRatingService.findAll();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга с id {}", id);
        return mpaRatingService.findById(id);
    }
}