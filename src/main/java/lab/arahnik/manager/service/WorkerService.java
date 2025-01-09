package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.administration.service.LogService;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.response.WorkerDto;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.enums.ChangeType;
import lab.arahnik.manager.repository.CoordinatesRepository;
import lab.arahnik.manager.repository.OrganizationRepository;
import lab.arahnik.manager.repository.PersonRepository;
import lab.arahnik.manager.repository.WorkerRepository;
import lab.arahnik.websocket.handler.TextSocketHandler;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class WorkerService {

  private final WorkerRepository workerRepository;
  private final TextSocketHandler textSocketHandler;
  private final UserService userService;
  private final CoordinatesRepository coordinatesRepository;
  private final OrganizationRepository organizationRepository;
  private final PersonRepository personRepository;
  private final LogService logService;
  private final UserRepository userRepository;

  public WorkerService(WorkerRepository workerRepository, TextSocketHandler textSocketHandler, UserService userService,
                       CoordinatesRepository coordinatesRepository, OrganizationRepository organizationRepository,
                       PersonRepository personRepository, LogService logService, UserRepository userRepository) {
    this.workerRepository = workerRepository;
    this.textSocketHandler = textSocketHandler;
    this.userService = userService;
    this.coordinatesRepository = coordinatesRepository;
    this.organizationRepository = organizationRepository;
    this.personRepository = personRepository;
    this.logService = logService;
    this.userRepository = userRepository;
  }

  public List<WorkerDto> allWorkers() {
    var workers = workerRepository.findAll();
    return getWorkerDtos(workers);
  }

  public List<WorkerDto> allWorkersPage(Pageable pageable) {
    var workers = workerRepository
            .findAll(pageable)
            .getContent();
    return getWorkerDtos(workers);
  }

  private List<WorkerDto> getWorkerDtos(List<Worker> workers) {
    List<WorkerDto> res = new ArrayList<>();
    for (var worker : workers) {
      res.add(
              WorkerDto
                      .builder()
                      .id(worker.getId())
                      .name(worker.getName())
                      .coordinates(worker.getCoordinates())
                      .creationDate(worker.getCreationDate())
                      .organizationId(worker
                              .getOrganization()
                              .getId())
                      .salary(worker.getSalary())
                      .rating(worker.getRating())
                      .position(worker.getPosition())
                      .status(worker.getStatus())
                      .personId(worker
                              .getPerson()
                              .getId())
                      .ownerId(worker
                              .getOwner()
                              .getId())
                      .build());

    }
    return res;
  }

  public WorkerDto getWorkerById(Long id) {
    var worker = workerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Worker with id " + id + " not found"));
    return WorkerDto
            .builder()
            .id(worker.getId())
            .name(worker.getName())
            .coordinates(worker.getCoordinates())
            .creationDate(worker.getCreationDate())
            .organizationId(worker
                    .getOrganization()
                    .getId())
            .salary(worker.getSalary())
            .rating(worker.getRating())
            .position(worker.getPosition())
            .status(worker.getStatus())
            .personId(worker
                    .getPerson()
                    .getId())
            .ownerId(worker
                    .getOwner()
                    .getId())
            .build();
  }

  public WorkerDto createWorker(Worker newWorker) {
    var worker = workerRepository.save(newWorker);
    var organization = worker.getOrganization();
    organization.setEmployeesCount(organization.getEmployeesCount() + 1);
    organizationRepository.save(organization);
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(Worker.class.getSimpleName())
                    .type(ChangeType.CREATION)
                    .build()
                    .toString()
    );
    logService.addLog(ChangeType.CREATION, worker);
    return WorkerDto
            .builder()
            .id(worker.getId())
            .name(worker.getName())
            .coordinates(worker.getCoordinates())
            .creationDate(worker.getCreationDate())
            .organizationId(worker
                    .getOrganization()
                    .getId())
            .salary(worker.getSalary())
            .rating(worker.getRating())
            .position(worker.getPosition())
            .status(worker.getStatus())
            .personId(worker
                    .getPerson()
                    .getId())
            .ownerId(worker
                    .getOwner()
                    .getId())
            .isEditableByAdmin(worker.getEditableByAdmin())
            .build();
  }

  public WorkerDto updateWorker(WorkerDto workerDto) {
    var worker = workerRepository
            .findById(workerDto.getId())
            .orElseThrow(() -> new EntityNotFoundException("Worker with id " + workerDto.getId() + " not found"));

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, worker);

    worker.setName(workerDto.getName());

    if (!workerDto
            .getCoordinates()
            .getX()
            .equals(worker
                    .getCoordinates()
                    .getX()) ||
            !workerDto
                    .getCoordinates()
                    .getY()
                    .equals(worker
                            .getCoordinates()
                            .getY())) {
      worker.setCoordinates(
              coordinatesRepository.save(workerDto.getCoordinates())
      );
    }

    if (!workerDto
            .getOrganizationId()
            .equals(worker
                    .getOrganization()
                    .getId())) {
      updateOrganizationEmployeeCount(worker
              .getOrganization()
              .getId(), -1);
      worker.setOrganization(
              organizationRepository
                      .findById(workerDto.getOrganizationId())
                      .orElseThrow(() -> new EntityNotFoundException(
                              "Organization with id " + workerDto.getOrganizationId() + " not found"))
      );
      updateOrganizationEmployeeCount(workerDto.getOrganizationId(), 1);
    }

    worker.setSalary(workerDto.getSalary());
    worker.setRating(workerDto.getRating());
    worker.setPosition(workerDto.getPosition());
    worker.setStatus(workerDto.getStatus());
    worker.setPerson(
            personRepository
                    .findById(workerDto.getPersonId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Person with id " + workerDto.getPersonId() + " not found"))
    );

    var updatedWorker = workerRepository.save(worker);

    sendEvent(ChangeType.UPDATE, Worker.class.getSimpleName());
    logService.addLog(ChangeType.UPDATE, updatedWorker);

    return mapToWorkerDto(updatedWorker);
  }

  public void deleteWorker(Long id) {
    var worker = workerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Worker with id " + id + " not found"));

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, worker);

    updateOrganizationEmployeeCount(worker
            .getOrganization()
            .getId(), -1);
    workerRepository.deleteById(id);

    sendEvent(ChangeType.DELETION, Worker.class.getSimpleName());
  }

  private WorkerDto mapToWorkerDto(Worker worker) {
    return WorkerDto
            .builder()
            .id(worker.getId())
            .name(worker.getName())
            .coordinates(worker.getCoordinates())
            .creationDate(worker.getCreationDate())
            .organizationId(worker
                    .getOrganization()
                    .getId())
            .salary(worker.getSalary())
            .rating(worker.getRating())
            .position(worker.getPosition())
            .status(worker.getStatus())
            .personId(worker
                    .getPerson()
                    .getId())
            .ownerId(worker
                    .getOwner()
                    .getId())
            .build();
  }

  private void validateEditingRights(User user, Worker worker) {
    if (!Objects.equals(user.getId(), worker
            .getOwner()
            .getId()) &&
            !(user.getRole() == Role.ADMIN && worker.getEditableByAdmin())) {
      throw new InsufficientEditingRightsException("You are not owner of this worker");
    }
  }

  private void sendEvent(ChangeType changeType, String objectType) {
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(objectType)
                    .type(changeType)
                    .build()
                    .toString()
    );
  }

  private void updateOrganizationEmployeeCount(Long organizationId, int delta) {
    var organization = organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new EntityNotFoundException("Organization with id " + organizationId + " not found"));
    organization.setEmployeesCount(organization.getEmployeesCount() + delta);
    organizationRepository.save(organization);
  }

  private User getCurrentUserOrThrow() {
    var userId = userService.getCurrentUserId();
    return userRepository
            .findById(userId)
            .orElseThrow(
                    () -> new EntityNotFoundException("User not found")
            );
  }

}
