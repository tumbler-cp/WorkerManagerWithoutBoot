package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.enums.ChangeType;
import lab.arahnik.manager.repository.LocationRepository;
import lab.arahnik.websocket.handler.TextSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final TextSocketHandler textSocketHandler;

    public List<LocationDto> allLocations() {
        var locations = locationRepository.findAll();
        return getLocationDtos(locations);
    }

    public List<LocationDto> allLocationsPage(Pageable pageable) {
        var locations = locationRepository.findAll(pageable).getContent();
        return getLocationDtos(locations);
    }

    private List<LocationDto> getLocationDtos(List<Location> locations) {
        List<LocationDto> res = new ArrayList<>(locations.size());
        for (var location : locations) {
            res.add(
                    LocationDto.builder()
                            .id(location.getId())
                            .x(location.getX())
                            .y(location.getY())
                            .name(location.getName())
                            .ownerId(location.getOwner().getId())
                            .build()
            );
        }
        return res;
    }

    public LocationDto getLocationById(Long id) {
        var location = locationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Location not found"));
        return LocationDto.builder()
                .id(location.getId())
                .x(location.getX())
                .y(location.getY())
                .name(location.getName())
                .ownerId(location.getOwner().getId())
                .build();
    }

    public LocationDto createLocation(Location location) {
        var res = locationRepository.save(location);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Location.class.getSimpleName())
                        .type(ChangeType.CREATION)
                        .build().toString());
        var userId = userService.getCurrentUserId();
        return LocationDto.builder()
                .id(res.getId())
                .x(res.getX())
                .y(res.getY())
                .name(res.getName())
                .ownerId(userId)
                .build();
    }

    public LocationDto updateLocation(LocationDto locationDto) {
        var location = locationRepository.findById(locationDto.getId()).orElseThrow(
                () -> new EntityNotFoundException("Location not found")
        );
        var userId = userService.getCurrentUserId();
        if (!Objects.equals(userId, location.getOwner().getId())) {
            throw new InsufficientEditingRightsException("You do not have permission to update this location");
        }
        location.setX(locationDto.getX());
        location.setY(locationDto.getY());
        location.setName(locationDto.getName());
        var res = locationRepository.save(location);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Location.class.getSimpleName())
                        .type(ChangeType.UPDATE)
                        .build().toString());
        return LocationDto.builder()
                .id(res.getId())
                .x(res.getX())
                .y(res.getY())
                .name(res.getName())
                .ownerId(userId)
                .build();

    }

    public void deleteLocation(Long id) {
        var userId = userService.getCurrentUserId();
        if (locationRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Location not found");
        }
        if (!Objects.equals(userId, locationRepository.findById(id).get().getOwner().getId())) {
            throw new InsufficientEditingRightsException("You do not have permission to delete this location");
        }
        locationRepository.deleteById(id);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Location.class.getSimpleName())
                        .type(ChangeType.DELETION)
                        .build().toString());
    }
}
