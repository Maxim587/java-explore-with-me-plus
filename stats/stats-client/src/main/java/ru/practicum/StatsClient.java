package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class StatsClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String baseUrl;
    private final RestClient restClient;

    @Autowired
    public StatsClient(@Value("${statsServer.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    log.error(
                            "Api request was failed. Response status: {}, body: {}",
                            response.getStatusCode(),
                            new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)
                    );
                }))
                .build();
    }

    public EndpointHitDto hit(NewEndpointHitDto hitDto) {
        log.info("Клиентом статистики stats-client получен запрос на добавление записи hitDto={}", hitDto);
        EndpointHitDto resp = restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/hit").build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(EndpointHitDto.class);
        log.info("Клиентом статистики stats-client получен ответ от сервера {}", resp);
        return resp;
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Клиентом статистики stats-client получен запрос на предоставление статистики hitDto={}, hitDto={}, hitDto={}, hitDto={}", start, end, uris, unique);
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
