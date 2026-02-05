package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.NewEndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper mapper;

    @Override
    @Transactional
    public EndpointHitDto createEndpointHit(NewEndpointHitDto newEndpointHitDto) {
        return mapper.mapToEndpointHitDto(statsRepository.save(mapper.mapToEndpointHit(newEndpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkDates(start, end);

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.getStatsAllWithUniqueIps(start, end);
            }
            return statsRepository.getStatsAll(start, end);
        } else if (unique) {
            return statsRepository.getStatsByUriListWithUniqueIps(start, end, uris);
        }

        return statsRepository.getStatsByUriList(start, end, uris);
    }

    private void checkDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ConditionsNotMetException("Начальная дата не может быть позже конечной");
        }
    }
}
