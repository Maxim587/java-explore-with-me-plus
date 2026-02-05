package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParticipationRequestMapper {

    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request);
}
