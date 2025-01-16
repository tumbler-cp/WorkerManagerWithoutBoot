package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewOrganization;
import lab.arahnik.manager.dto.response.OrganizationDto;
import lab.arahnik.manager.entity.Address;
import lab.arahnik.manager.entity.Location;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.importer.service.FileLogService;
import lab.arahnik.manager.importer.service.OrganizationImportService;
import lab.arahnik.manager.repository.AddressRepository;
import lab.arahnik.manager.service.OrganizationService;
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

import java.util.List;

@RestController
@RequestMapping("/organization")
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
public class OrganizationController {

  private final UtilComponent utilComponent;
  @Value("${minio.bucket-name}")
  private String bucket;

  private final OrganizationService organizationService;
  private final UserService userService;
  private final AddressRepository addressRepository;
  private final OrganizationImportService organizationImportService;
  private final FileLogService fileLogService;
  private final MinioService minioService;

  @GetMapping("/all")
  public List<OrganizationDto> allOrganizations() {
    return organizationService.all();
  }

  @GetMapping("/find")
  public OrganizationDto findOrganizationById(@RequestParam("id") Long id) {
    return organizationService.getById(id);
  }

  @GetMapping("/paged")
  public List<OrganizationDto> getWorkersByPage(
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
          @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
  ) {
    Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
    return organizationService.page(pageable);
  }

  @PostMapping("/new")
  public OrganizationDto createOrganization(@RequestBody NewOrganization newOrganization) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    return organizationService.create(
            Organization
                    .builder()
                    .address(
                            addressRepository.save(
                                    Address
                                            .builder()
                                            .zipCode(newOrganization.getZipCode())
                                            .build()
                            )
                    )
                    .annualTurnover(newOrganization.getAnnualTurnover())
                    .employeesCount(newOrganization.getEmployeesCount())
                    .fullName(newOrganization.getFullName())
                    .rating(newOrganization.getRating())
                    .owner(user)
                    .editableByAdmin(newOrganization.getEditableByAdmin())
                    .build()
    );
  }

  @PostMapping("/upload")
  public List<OrganizationDto> uploadLocation(@RequestParam("file") MultipartFile file) throws Exception {
    String tempFilePath = utilComponent.getTmpFilePath(file);
    utilComponent.transfer(file, tempFilePath);
    String fileName = null;
    try {
      var res = organizationImportService.importFrom(tempFilePath);
      fileName = minioService.saveFile(bucket,
              SecurityContextHolder.getContext()
                      .getAuthentication()
                      .getName(),
              file);
      fileLogService.save(fileName, res.size(), Organization.class.getSimpleName());
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
  public OrganizationDto updateOrganization(@RequestBody OrganizationDto organizationDto) {
    return organizationService.update(organizationDto);
  }

  @DeleteMapping("/delete")
  public void deleteOrganization(@RequestParam("id") Long id) {
    organizationService.delete(id);
  }

  @ExceptionHandler({InsufficientEditingRightsException.class, EntityNotFoundException.class})
  public ResponseEntity<String> handleException(Exception exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

}
