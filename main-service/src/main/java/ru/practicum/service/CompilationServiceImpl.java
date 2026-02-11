package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository repository;
    private final EventRepository eventRepository;
    private final CompilationMapper mapper;

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!repository.existsById(compId)) {
            throw new NotFoundException("Подборка с id " + compId + " не найдена");
        }
        repository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto dto) {
        Set<Event> events = getEvents(dto.getEvents());
        return mapper.mapCompilationToCompilationDto(repository.save(mapper.mapNewCompilationDtoToCompilation(dto, events)));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        updateCompilationFields(compilation, updateRequest);
        return mapper.mapCompilationToCompilationDto(repository.save(compilation));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        if (pinned != null && pinned) {
            return repository.findAllByPinned(true, page).stream()
                    .map(mapper::mapCompilationToCompilationDto)
                    .toList();
        }

        return repository.getCompilationList(page).stream()
                .map(mapper::mapCompilationToCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        return mapper.mapCompilationToCompilationDto(compilation);
    }

    private Set<Event> getEvents(Set<Long> eventIds) {
        Set<Event> events = new HashSet<>();
        if (eventIds != null && !eventIds.isEmpty()) {
            events.addAll(eventRepository.findAllByIdIn(eventIds));
            if (eventIds.size() > events.size()) {
                throw new NotFoundException("В переданном списке событий есть события, которые не найдены в системе");
            }
        }
        return events;
    }

    private void updateCompilationFields(Compilation compilation, UpdateCompilationRequest updateRequest) {
        if (updateRequest.getEvents() != null) {
            compilation.setEvents(getEvents(updateRequest.getEvents()));
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
    }
}
