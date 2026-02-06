package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.Location;
import ru.practicum.model.EventLocation;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {
    EventLocation mapLocationToEventLocation(Location location);

    Location mapEventLocationToLocation(EventLocation eventLocation);
}
