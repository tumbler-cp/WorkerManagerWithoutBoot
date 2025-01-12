package lab.arahnik.manager.importer.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationImportService {

  private final UserService userService;
  private final LocationService locationService;

  public List<LocationDto> importLocations(String filePath) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    List<Location> result = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        Location location = Location
                .builder()
                .x(Double.parseDouble(nextLine[0]))
                .y(Long.parseLong(nextLine[1]))
                .name(nextLine[2])
                .editableByAdmin(Boolean.parseBoolean(nextLine[3]))
                .owner(user)
                .build();
        locationService.validateLocation(location);
        result.add(location);
      }
    } catch (IOException | CsvValidationException e) {
      System.out.println(e.getMessage());
    }
    return locationService.saveAllLocations(result);
  }

}
