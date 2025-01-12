package lab.arahnik.manager.importer.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.dto.response.LocationDto;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.entity.Person;
import lab.arahnik.manager.service.LocationService;
import lab.arahnik.manager.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonImportService {
  private final UserService userService;
  private final PersonService personService;

  public List<LocationDto> importLocations(String filePath) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    List<Person> result = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        Person person = Person
                .builder()
                .build();
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
