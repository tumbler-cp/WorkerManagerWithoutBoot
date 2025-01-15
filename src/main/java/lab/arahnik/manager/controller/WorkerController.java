package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewWorker;
import lab.arahnik.manager.dto.response.WorkerDto;
import lab.arahnik.manager.entity.Coordinates;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.importer.service.WorkerImportService;
import lab.arahnik.manager.repository.CoordinatesRepository;
import lab.arahnik.manager.repository.OrganizationRepository;
import lab.arahnik.manager.repository.PersonRepository;
import lab.arahnik.manager.service.WorkerService;
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
@RequestMapping("/worker")
@RequiredArgsConstructor
public class WorkerController {

  private final WorkerService workerService;
  private final UserService userService;
  private final CoordinatesRepository coordinatesRepository;
  private final PersonRepository personRepository;
  private final OrganizationRepository organizationRepository;
  private final WorkerImportService workerImportService;

  @GetMapping("/all")
  public List<WorkerDto> allWorkers() {
    return workerService.allWorkers();
  }

  @GetMapping("/find")
  public WorkerDto findWorkerById(@RequestParam("id") Long id) {
    return workerService.getWorkerById(id);
  }

  @GetMapping("/paged")
  public List<WorkerDto> getWorkersByPage(
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
          @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
  ) {
    Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
    return workerService.allWorkersPage(pageable);
  }

  @PostMapping("/new")
  public WorkerDto createWorker(@RequestBody NewWorker newWorker) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    var person = personRepository
            .findById(newWorker.getPersonId())
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    var organization = organizationRepository
            .findById(newWorker.getOrganizationId())
            .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    return workerService.createWorker(
            Worker
                    .builder()
                    .name(newWorker.getName())
                    .coordinates(
                            coordinatesRepository.save(
                                    Coordinates
                                            .builder()
                                            .x(newWorker.getX())
                                            .y(newWorker.getY())
                                            .build()
                            )
                    )
                    .organization(
                            organization
                    )
                    .salary(newWorker.getSalary())
                    .rating(newWorker.getRating())
                    .position(newWorker.getPosition())
                    .status(newWorker.getStatus())
                    .person(person)
                    .owner(user)
                    .editableByAdmin(newWorker.getEditableByAdmin())
                    .build()
    );
  }

  @PostMapping("/upload")
  public List<WorkerDto> uploadLocation(@RequestParam("file") MultipartFile file) throws IOException {
    String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
    file.transferTo(new File(tempFilePath));
    return workerImportService.importWorkers(tempFilePath);
  }

  @PutMapping("/update")
  public WorkerDto updateWorker(@RequestBody WorkerDto workerDto) {
    return workerService.updateWorker(workerDto);
  }

  @DeleteMapping("/delete")
  public void deleteWorker(@RequestParam("id") Long id) {
    workerService.deleteWorker(id);
  }

  @ExceptionHandler({InsufficientEditingRightsException.class, EntityNotFoundException.class})
  public ResponseEntity<String> handleException(Exception exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

}
