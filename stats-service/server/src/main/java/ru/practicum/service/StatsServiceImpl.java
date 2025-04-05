package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.StatsRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private StatsRepository statsRepository;

    @Override
    public Hit addHit(HitDto hitDto) {
        return statsRepository.save(StatsMapper.toHit(hitDto));
    }

    @Override
    public List<StatsDto> findStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startTime = parseDateTime(start);
        LocalDateTime endTime = parseDateTime(end);

        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Время начала перед временм конца");
        }

        boolean isUnique = Boolean.TRUE.equals(unique);

        if (uris != null && !uris.isEmpty()) {
            return isUnique
                    ? statsRepository.findUniqueIpHitsWithUris(startTime, endTime, uris)
                    : statsRepository.findHitsWithUris(startTime, endTime, uris);
        }

        return isUnique
                ? statsRepository.findUniqueIpHits(startTime, endTime)
                : statsRepository.findHits(startTime, endTime);
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            String decoded = URLDecoder.decode(dateStr, StandardCharsets.UTF_8);
            return LocalDateTime.parse(decoded, DATE_TIME_PATTERN);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new ValidationException("Период времени передать как 'yyyy-MM-dd HH:mm:ss'");
        }
    }
}
