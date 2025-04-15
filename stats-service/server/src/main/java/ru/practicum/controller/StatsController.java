package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.model.Hit;
import ru.practicum.service.StatsService;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping
public class StatsController {
    private static final String HIT_PREFIX = "/hit";
    private static final String STATS_PREFIX = "/stats";
    private final StatsService statsService;

    @PostMapping(HIT_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public Hit addHit(@RequestBody HitDto requestDto) {
        return statsService.addHit(requestDto);
    }

    @GetMapping(STATS_PREFIX)
    public List<StatsDto> findStats(@RequestParam String start,
                                    @RequestParam String end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") Boolean unique) {
        return statsService.findStats(start, end, uris, unique);
    }
}
