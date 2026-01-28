package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class StatsClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String baseUrl;
    private final RestClient restClient;

    public StatsClient(@Value("http://localhost:9090") String baseUrl) {
        this.baseUrl = baseUrl;
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public EndpointHitDto hit(NewEndpointHitDto newEndpointHitDto) {
        return restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newEndpointHitDto)
                .retrieve()
                .body(EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/stats")
                .queryParam("start", start.format(DATE_TIME_FORMATTER))
                .queryParam("end", end.format(DATE_TIME_FORMATTER))
                .queryParamIfPresent("unique", Optional.ofNullable(unique))
                .queryParamIfPresent("uris", Optional.ofNullable(CollectionUtils.isEmpty(uris) ? null : uris))
                .build()
                .toUri();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
