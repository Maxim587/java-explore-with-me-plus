package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class StatsClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //    private final String baseUrl;
//    private final RestClient restClient;
    private final String baseUrl = "http://localhost:9090";
    private final RestTemplate restTemplate = new RestTemplate();

//    public StatsClient(@Value("http://localhost:9090") String baseUrl) {
//        this.baseUrl = baseUrl;
//        restClient = RestClient.builder()
//                .baseUrl(baseUrl)
//                .defaultStatusHandler(HttpStatusCode::is5xxServerError, ((request, response) -> {
//                    log.error(
//                            "Api request was failed. Response status: {}, body: {}",
//                            response.getStatusCode(),
//                            new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)
//                    );
//                }))
//                .build();
//    }

    public EndpointHitDto hit(NewEndpointHitDto hit) {
        HttpEntity<NewEndpointHitDto> requestEntity = new HttpEntity<>(hit);
        return restTemplate.exchange(baseUrl + "/hit", HttpMethod.POST, requestEntity, EndpointHitDto.class).getBody();
    }

//    public ResponseEntity<Object> hit(NewEndpointHitDto hitDto) {
//        log.info("Клиентом статистики stats-client получен запрос на добавление записи hitDto={}", hitDto);
//        ResponseEntity<Object> resp = restClient.post()
//                .uri(uriBuilder -> uriBuilder.path("/hit").build())
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(hitDto)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .toEntity(Object.class);
//        log.info("Клиентом статистики stats-client получен ответ от сервера ResponseEntity<Object>={}", resp);
//        return resp;
//    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Клиентом статистики stats-client получен запрос на получение статистики hitDto={}, hitDto={}, hitDto={}, hitDto={}", start, end, uris, unique);
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/stats")
                .queryParam("start", start.format(DATE_TIME_FORMATTER))
                .queryParam("end", end.format(DATE_TIME_FORMATTER))
                .queryParamIfPresent("unique", Optional.ofNullable(unique))
                .queryParamIfPresent("uris", Optional.ofNullable(CollectionUtils.isEmpty(uris) ? null : uris))
                .build()
                .toUri();

//        return restClient.get()
//                .uri(uri)
//                .retrieve()
//                .body(new ParameterizedTypeReference<>() {
//                });
        ViewStatsDto[] response = restTemplate.getForObject(uri, ViewStatsDto[].class);
        return response != null ? List.of(response) : Collections.emptyList();
    }
}
