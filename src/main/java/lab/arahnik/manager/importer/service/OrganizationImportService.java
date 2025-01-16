package lab.arahnik.manager.importer.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.dto.response.OrganizationDto;
import lab.arahnik.manager.entity.Address;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.repository.AddressRepository;
import lab.arahnik.manager.service.OrganizationService;
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
public class OrganizationImportService {

  private final UserService userService;
  private final OrganizationService organizationService;
  private final AddressRepository addressRepository;

  @Transactional
  public List<OrganizationDto> importFrom(String filePath) {
    var user = userService.getByUsername(
            SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName()
    );
    List<Organization> result = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        Organization organization = Organization
                .builder()
                .address(addressRepository.save(Address.builder()
                        .zipCode(nextLine[0])
                        .build()))
                .annualTurnover(Float.parseFloat(nextLine[1]))
                .employeesCount(1L)
                .fullName(nextLine[2])
                .rating(Float.parseFloat(nextLine[3]))
                .owner(user)
                .editableByAdmin(Boolean.parseBoolean(nextLine[4]))
                .build();
        organizationService.validateOrganization(organization);
        result.add(organization);
      }
    } catch (IOException | CsvValidationException e) {
      System.out.println(e.getMessage());
    }
    return organizationService.saveAll(result);
  }

}
