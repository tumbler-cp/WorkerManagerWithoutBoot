package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.enums.ChangeType;
import lab.arahnik.manager.repository.LocationRepository;
import lab.arahnik.websocket.handler.TextSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationRepository locationRepository;
  private final UserService userService;
  private final TextSocketHandler textSocketHandler;
  private final UserRepository userRepository;
  private final Validator validator;

  public List<LocationDto> allLocations() {
    var locations = locationRepository.findAll();
    return getLocationDtos(locations);
  }

  public List<LocationDto> allLocationsPage(Pageable pageable) {
    var locations = locationRepository
            .findAll(pageable)
            .getContent();
    return getLocationDtos(locations);
  }

  private List<LocationDto> getLocationDtos(List<Location> locations) {
    List<LocationDto> res = new ArrayList<>(locations.size());
    for (var location : locations) {
      res.add(
              LocationDto
                      .builder()
                      .id(location.getId())
                      .x(location.getX())
                      .y(location.getY())
                      .name(location.getName())
                      .ownerId(location
                              .getOwner()
                              .getId())
                      .isEditableByAdmin(location.getEditableByAdmin())
                      .build()
      );
    }
    return res;
  }

  public LocationDto getLocationById(Long id) {
    var location = locationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Location not found"));
    return LocationDto
            .builder()
            .id(location.getId())
            .x(location.getX())
            .y(location.getY())
            .name(location.getName())
            .ownerId(location
                    .getOwner()
                    .getId())
            .build();
  }

  public LocationDto createLocation(Location location) {
    validateLocation(location);
    var res = locationRepository.save(location);
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(Location.class.getSimpleName())
                    .type(ChangeType.CREATION)
                    .build()
                    .toString());
    var userId = userService.getCurrentUserId();
    return LocationDto
            .builder()
            .id(res.getId())
            .x(res.getX())
            .y(res.getY())
            .name(res.getName())
            .ownerId(userId)
            .build();
  }

  public LocationDto updateLocation(LocationDto locationDto) {
    var location = locationRepository
            .findById(locationDto.getId())
            .orElseThrow(
                    () -> new EntityNotFoundException("Location not found")
            );
    var user = getCurrentUserOrThrow();
    validateEditingRights(user, location);

    location.setX(locationDto.getX());
    location.setY(locationDto.getY());
    location.setName(locationDto.getName());

    validateLocation(location);

    var updatedLocation = locationRepository.save(location);

    sendEvent(ChangeType.UPDATE, Location.class.getSimpleName());

    return mapToDto(updatedLocation, user.getId());
  }

  public void deleteLocation(Long id) {
    var location = locationRepository
            .findById(id)
            .orElseThrow(
                    () -> new EntityNotFoundException("Location not found")
            );

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, location);

    locationRepository.deleteById(id);

    sendEvent(ChangeType.DELETION, Location.class.getSimpleName());
  }

  private User getCurrentUserOrThrow() {
    var userId = userService.getCurrentUserId();
    return userRepository
            .findById(userId)
            .orElseThrow(
                    () -> new EntityNotFoundException("User not found")
            );
  }

  private void validateEditingRights(User user, Location location) {
    if (!Objects.equals(user.getId(), location
            .getOwner()
            .getId()) &&
            !(user.getRole() == Role.ADMIN && location.getEditableByAdmin())) {
      throw new InsufficientEditingRightsException("You do not have permission to modify this location");
    }
  }

  private void sendEvent(ChangeType changeType, String objectType) {
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(objectType)
                    .type(changeType)
                    .build()
                    .toString()
    );
  }

  private LocationDto mapToDto(Location location, Long ownerId) {
    return LocationDto
            .builder()
            .id(location.getId())
            .x(location.getX())
            .y(location.getY())
            .name(location.getName())
            .ownerId(ownerId)
            .build();
  }

  public void validateLocation(Location location) {
    Set<ConstraintViolation<Location>> violations = validator.validate(location);

    if (!violations.isEmpty()) {
      StringBuilder message = new StringBuilder();
      for (ConstraintViolation<Location> violation : violations) {
        message
                .append(violation.getMessage())
                .append("\n");
      }
      throw new RuntimeException(message.toString());
    }
  }

}
