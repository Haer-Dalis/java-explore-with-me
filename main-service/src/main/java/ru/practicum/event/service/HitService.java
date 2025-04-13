package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.additions.Constants;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HitService {
    private static final String APP_NAME = "ewm-main-service";

    @Transactional
    public HitDto createHitDto(HttpServletRequest request) {
        return HitDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().withNano(0).format(Constants.DATE_TIME_FORMATTER))
                .build();
    }
}
