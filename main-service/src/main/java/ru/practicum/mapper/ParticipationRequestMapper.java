package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParticipationRequestMapper {

    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request);


//    static ParticipationRequestDto requestToParticipationRequestDto(ParticipationRequest request) {
//        ParticipationRequestDto requestDto = new ParticipationRequestDto();
//        requestDto.setId(request.getId());
//        requestDto.setEvent(request.getEvent().getId());
//        requestDto.setRequester(request.getRequester().getId());
//        requestDto.setCreated(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(request.getCreated()));
//        requestDto.setStatus(request.getStatus().name());
//        return requestDto;
//    }
}
