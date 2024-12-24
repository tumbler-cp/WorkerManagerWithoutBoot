package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewLocation;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final UserService userService;

    @GetMapping("/all")
    public List<LocationDto> allLocations() {
        return locationService.allLocations();
    }

    @GetMapping
    public LocationDto getLocationById(@RequestParam(name = "id") Long id) {
        return locationService.getLocationById(id);
    }

    @PostMapping("/new")
    public LocationDto createLocation(@RequestBody NewLocation newLocation) {
        var user = userService.getByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        return locationService.createLocation(
                Location.builder()
                        .x(newLocation.getX())
                        .y(newLocation.getY())
                        .name(newLocation.getName())
                        .owner(user)
                        .build()
        );
    }

    @PutMapping("/update")
    public LocationDto updateLocation(@RequestBody LocationDto location) {
        return locationService.updateLocation(location);
    }

    @DeleteMapping("/delete")
    public void deleteLocation(@RequestParam(name = "id") Long id) {
        locationService.deleteLocation(id);
    }

    @ExceptionHandler({InsufficientEditingRightsException.class, EntityNotFoundException.class})
    public ResponseEntity<String> handleException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
