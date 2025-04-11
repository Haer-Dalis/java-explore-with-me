package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class StatsClient  {
    private final RestTemplate rest;
    private final String statsUrl;

    public StatsClient(RestTemplate rest, @Value("${stats-gateway.url}") String statsUrl) {
        this.rest = rest;
        this.statsUrl = statsUrl;
    }

    public void createHit(HitDto statRequestDto) {
        HttpEntity<HitDto> requestEntity = new HttpEntity<>(statRequestDto);
        rest.postForEntity(statsUrl + "/hit", requestEntity, Void.class);
    }

    public StatsDto findStats(String start, String end, List<String> uris, Boolean unique) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(statsUrl + "/stats")
                .queryParam("start", start).encode(StandardCharsets.UTF_8)
                .queryParam("end", end).encode(StandardCharsets.UTF_8)
                .queryParam("uris", uris)
                .queryParam("unique", unique);
        return rest.getForObject(builder.toUriString(), StatsDto.class);
    }

    public List<StatsDto> getStatsByDateAndUris(String start, String end, List<String> uris, Boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(statsUrl + "/stats")
                .queryParam("start", start).encode(StandardCharsets.UTF_8)
                .queryParam("end", end).encode(StandardCharsets.UTF_8)
                .queryParam("uris", uris)
                .queryParam("unique", unique);
        ResponseEntity<List<StatsDto>> responseEntity = rest.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<StatsDto>>() {});

        return responseEntity.getBody();
    }
}
