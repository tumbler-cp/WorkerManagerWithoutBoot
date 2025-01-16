package lab.arahnik.manager.importer.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.dto.response.PersonDto;
import lab.arahnik.manager.entity.Person;
import lab.arahnik.manager.enums.Color;
import lab.arahnik.manager.repository.LocationRepository;
import lab.arahnik.manager.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonImportService {

  private final UserService userService;
  private final LocationRepository locationRepository;
  private final PersonService personService;

  @Transactional
  public List<PersonDto> importFrom(String filePath) {
    var user = userService.getByUsername(
            SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName()
    );
    List<Person> result = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        String eyeColorStr = nextLine[0].trim()
                .toUpperCase();
        String hairColorStr = nextLine[1].trim()
                .toUpperCase();
        System.out.println(
                "Processing eyeColor: " + eyeColorStr + ", hairColor: " + hairColorStr); // Add this line for debugging

        Person person = Person.builder()
                .eyeColor(Color.valueOf(eyeColorStr))
                .hairColor(Color.valueOf(hairColorStr))
                .location(locationRepository.findById(Long.parseLong(nextLine[2]))
                        .orElseThrow(
                                () -> new EntityNotFoundException("Location not found")
                        ))
                .height(Double.parseDouble(nextLine[3]))
                .weight(Long.parseLong(nextLine[4]))
                .passportID(nextLine[5])
                .owner(user)
                .editableByAdmin(Boolean.parseBoolean(nextLine[6]))
                .build();
        personService.validatePerson(person);
        result.add(person);
      }
    } catch (CsvValidationException | IOException e) {
      System.out.println(e.getMessage());
    }
    return personService.saveAll(result);
  }

}
