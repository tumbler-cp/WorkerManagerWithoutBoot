package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewLocation;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.importer.service.FileLogService;
import lab.arahnik.manager.importer.service.LocationImportService;
import lab.arahnik.manager.service.LocationService;
import lab.arahnik.minio.MinioService;
import lab.arahnik.util.service.UtilComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/location")
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
public class LocationController {

  private final UtilComponent utilComponent;
  @Value("${minio.bucket-name}")
  private String bucket;

  private final LocationService locationService;
  private final LocationImportService locationImportService;
  private final UserService userService;
  private final FileLogService fileLogService;
  private final MinioService minioService;

  @GetMapping("/all")
  public List<LocationDto> allLocations() {
    return locationService.all();
  }

  @GetMapping("/find")
  public LocationDto getLocationById(@RequestParam(name = "id") Long id) {
    return locationService.getById(id);
  }

  @GetMapping("/paged")
  public List<LocationDto> getLocationsByPage(
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
          @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
  ) {
    Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
    return locationService.page(pageable);
  }

  @PostMapping("/new")
  public LocationDto createLocation(@RequestBody NewLocation newLocation) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    return locationService.create(
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
  public List<LocationDto> upload(@RequestParam("file") MultipartFile file) throws Exception {
    String tempFilePath = utilComponent.getTmpFilePath(file);
    utilComponent.transfer(file, tempFilePath);
    String fileName = null;
    try {
      var res = locationImportService.importFrom(tempFilePath);
      fileName = minioService.saveFile(bucket,
              SecurityContextHolder.getContext()
                      .getAuthentication()
                      .getName(),
              file);
      fileLogService.save(fileName, res.size(), Location.class.getSimpleName());
      return res;
    } catch (Exception e) {
      System.out.println(e.getMessage());

      if (fileName != null) {
        minioService.rollbackSaveFile(fileName, bucket);
      }

      throw e;
    }
  }


  @PutMapping("/update")
  public LocationDto updateLocation(@RequestBody LocationDto location) {
    return locationService.update(location);
  }

  @DeleteMapping("/delete")
  public void deleteLocation(@RequestParam(name = "id") Long id) {
    locationService.delete(id);
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
