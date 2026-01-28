package ru.practicum.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.EndpointHitDto;
import ru.practicum.NewEndpointHitDto;
import ru.practicum.model.EndpointHit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatsMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EndpointHit mapToEndpointHit(NewEndpointHitDto dto);


    EndpointHitDto mapToEndpointHitDto(EndpointHit endpointHit);
}
