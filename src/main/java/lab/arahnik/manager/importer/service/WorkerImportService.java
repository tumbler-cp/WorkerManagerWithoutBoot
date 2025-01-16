package lab.arahnik.manager.importer.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.dto.response.WorkerDto;
import lab.arahnik.manager.entity.Coordinates;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.entity.Person;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.enums.Position;
import lab.arahnik.manager.enums.Status;
import lab.arahnik.manager.repository.OrganizationRepository;
import lab.arahnik.manager.repository.PersonRepository;
import lab.arahnik.manager.service.WorkerService;
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
public class WorkerImportService {

  private final UserService userService;
  private final OrganizationRepository organizationRepository;
  private final PersonRepository personRepository;
  private final WorkerService workerService;

  @Transactional
  public List<WorkerDto> importFrom(String filePath) {
    var user = userService.getByUsername(SecurityContextHolder.getContext()
            .getAuthentication()
            .getName());

    if (user == null) {
      throw new RuntimeException("User must not be null");
    }

    List<Worker> result = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        Coordinates coordinates = Coordinates.builder()
                .x(Float.parseFloat(nextLine[1]))
                .y(Float.parseFloat(nextLine[2]))
                .build();

        Organization organization = organizationRepository.findById(Long.parseLong(nextLine[3]))
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Person person = personRepository.findById(Long.parseLong(nextLine[8]))
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));

        Worker worker = Worker.builder()
                .name(nextLine[0])
                .coordinates(coordinates)
                .organization(organization)
                .salary(Double.parseDouble(nextLine[4]))
                .rating(Long.parseLong(nextLine[5]))
                .position(Position.valueOf(nextLine[6]))
                .status(Status.valueOf(nextLine[7]))
                .person(person)
                .owner(user)
                .editableByAdmin(Boolean.parseBoolean(nextLine[9]))
                .build();

        result.add(worker);
      }
    } catch (IOException | CsvValidationException e) {
      System.out.println(e.getMessage());
    }
    return workerService.saveAll(result);
  }

}
