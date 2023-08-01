package ru.practicum.mapper;

import ru.practicum.dto.LocationDto;
import ru.practicum.model.Location;

public class LocationMapper {

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
