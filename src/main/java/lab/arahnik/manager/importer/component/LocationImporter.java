package lab.arahnik.manager.importer.component;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationImporter {

  private final UserService userService;
  private final LocationService locationService;

  public void importLocations(String filePath) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      List<String[]> rows = reader.readAll();
      for (String[] row : rows) {
        Location location = Location
                .builder()
                .x(Double.parseDouble(row[0]))
                .y(Long.parseLong(row[1]))
                .name(row[2])
                .owner(user)
                .editableByAdmin(Boolean.parseBoolean(row[3]))
                .build();
        locationService.createLocation(location);
      }
    } catch (IOException | CsvException e) {
      System.out.printf(e.getMessage());
    }
  }

}
