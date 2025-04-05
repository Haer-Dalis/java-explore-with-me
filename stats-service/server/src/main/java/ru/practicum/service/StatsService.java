package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.model.Hit;

import java.util.List;

public interface StatsService {
    Hit addHit(HitDto endpointHitDto);

    List<StatsDto> findStats(String start, String end, List<String> uris, Boolean unique);
}
