package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewLocation;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.importer.service.LocationImportService;
import lab.arahnik.manager.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

  private final LocationService locationService;
  private final LocationImportService locationImportService;
  private final UserService userService;

  @GetMapping("/all")
  public List<LocationDto> allLocations() {
    return locationService.allLocations();
  }

  @GetMapping("/find")
  public LocationDto getLocationById(@RequestParam(name = "id") Long id) {
    return locationService.getLocationById(id);
  }

  @GetMapping("/paged")
  public List<LocationDto> getLocationsByPage(
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
          @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
  ) {
    Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
    return locationService.allLocationsPage(pageable);
  }

  @PostMapping("/new")
  public LocationDto createLocation(@RequestBody NewLocation newLocation) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    return locationService.createLocation(
            Location
                    .builder()
                    .x(newLocation.getX())
                    .y(newLocation.getY())
                    .name(newLocation.getName())
                    .owner(user)
                    .editableByAdmin(newLocation.getEditableByAdmin())
                    .build()
    );
  }

  @PostMapping("/upload")
  public List<LocationDto> uploadLocation(@RequestParam("file") MultipartFile file) throws IOException {
    String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
    file.transferTo(new File(tempFilePath));
    return locationImportService.importLocations(tempFilePath);
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

  @ExceptionHandler({IOException.class})
  public ResponseEntity<String> handleIoException(Exception exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
