package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewWorker;
import lab.arahnik.manager.dto.response.WorkerDto;
import lab.arahnik.manager.entity.Coordinates;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.repository.CoordinatesRepository;
import lab.arahnik.manager.repository.OrganizationRepository;
import lab.arahnik.manager.repository.PersonRepository;
import lab.arahnik.manager.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/all")
    public List<WorkerDto> allWorkers() {
        return workerService.allWorkers();
    }

    @GetMapping("/find")
    public WorkerDto findWorkerById(@RequestParam("id") Long id) {
        return workerService.getWorkerById(id);
    }

    @PostMapping("/new")
    public WorkerDto createWorker(@RequestBody NewWorker newWorker) {
        var user = userService.getByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        var person = personRepository.findById(newWorker.getPersonId()).orElseThrow(() -> new EntityNotFoundException("Person not found"));
        var organization = organizationRepository.findById(newWorker.getOrganizationId()).orElseThrow(() -> new EntityNotFoundException("Organization not found"));
        return workerService.createWorker(
                Worker.builder()
                        .name(newWorker.getName())
                        .coordinates(
                                coordinatesRepository.save(
                                        Coordinates.builder()
                                                .x(newWorker.getX()).y(newWorker.getY()).build()
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
