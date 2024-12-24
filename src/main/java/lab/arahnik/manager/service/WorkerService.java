package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.administration.service.LogService;
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

    public WorkerService(WorkerRepository workerRepository, TextSocketHandler textSocketHandler, UserService userService, CoordinatesRepository coordinatesRepository, OrganizationRepository organizationRepository, PersonRepository personRepository, LogService logService) {
        this.workerRepository = workerRepository;
        this.textSocketHandler = textSocketHandler;
        this.userService = userService;
        this.coordinatesRepository = coordinatesRepository;
        this.organizationRepository = organizationRepository;
        this.personRepository = personRepository;
        this.logService = logService;
    }

    public List<WorkerDto> allWorkers() {
        var workers = workerRepository.findAll();
        List<WorkerDto> res = new ArrayList<>();
        for (var worker : workers) {
            res.add(
                    WorkerDto.builder()
                            .id(worker.getId())
                            .name(worker.getName())
                            .coordinates(worker.getCoordinates())
                            .creationDate(worker.getCreationDate())
                            .organizationId(worker.getOrganization().getId())
                            .salary(worker.getSalary())
                            .rating(worker.getRating())
                            .position(worker.getPosition())
                            .status(worker.getStatus())
                            .personId(worker.getPerson().getId())
                            .ownerId(worker.getOwner().getId())
                            .build()
            );
        }
        return res;
    }

    public WorkerDto getWorkerById(Long id) {
        var worker = workerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Worker with id " + id + " not found"));
        return WorkerDto.builder()
                .id(worker.getId())
                .name(worker.getName())
                .coordinates(worker.getCoordinates())
                .creationDate(worker.getCreationDate())
                .organizationId(worker.getOrganization().getId())
                .salary(worker.getSalary())
                .rating(worker.getRating())
                .position(worker.getPosition())
                .status(worker.getStatus())
                .personId(worker.getPerson().getId())
                .ownerId(worker.getOwner().getId())
                .build();
    }

    public WorkerDto createWorker(Worker newWorker) {
        var worker = workerRepository.save(newWorker);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Worker.class.getSimpleName())
                        .type(ChangeType.CREATION)
                        .build().toString()
        );
        logService.addLog(ChangeType.CREATION, worker);
        return WorkerDto.builder()
                .id(worker.getId())
                .name(worker.getName())
                .coordinates(worker.getCoordinates())
                .creationDate(worker.getCreationDate())
                .organizationId(worker.getOrganization().getId())
                .salary(worker.getSalary())
                .rating(worker.getRating())
                .position(worker.getPosition())
                .status(worker.getStatus())
                .personId(worker.getPerson().getId())
                .ownerId(worker.getOwner().getId())
                .build();
    }

    public WorkerDto updateWorker(WorkerDto workerDto) {
        var worker = workerRepository.findById(workerDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Worker with id " + workerDto.getId() + " not found"));
        var userId = userService.getCurrentUserId();
        if (!Objects.equals(worker.getOwner().getId(), userId)) {
            throw new InsufficientEditingRightsException("You are not owner of this worker");
        }
        worker.setName(workerDto.getName());
        if (!workerDto.getCoordinates().getX().equals(worker.getCoordinates().getX()) ||
        !workerDto.getCoordinates().getY().equals(worker.getCoordinates().getY())) {
            worker.setCoordinates(
                    coordinatesRepository.save(
                            workerDto.getCoordinates()
                    )
            );
        }
        worker.setOrganization(
                organizationRepository.findById(workerDto.getOrganizationId())
                        .orElseThrow(() -> new EntityNotFoundException("Organization with id " + workerDto.getOrganizationId() + " not found"))
        );
        worker.setSalary(workerDto.getSalary());
        worker.setRating(workerDto.getRating());
        worker.setPosition(workerDto.getPosition());
        worker.setStatus(workerDto.getStatus());
        worker.setPerson(
                personRepository.findById(workerDto.getPersonId())
                        .orElseThrow(() -> new EntityNotFoundException("Person with id " + workerDto.getPersonId() + " not found"))
        );
        var res = workerRepository.save(worker);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Worker.class.getSimpleName())
                        .type(ChangeType.UPDATE)
                        .build().toString()
        );
        logService.addLog(ChangeType.UPDATE, worker);
        return WorkerDto.builder()
                .id(res.getId())
                .name(res.getName())
                .coordinates(res.getCoordinates())
                .creationDate(res.getCreationDate())
                .organizationId(res.getOrganization().getId())
                .salary(res.getSalary())
                .rating(res.getRating())
                .position(res.getPosition())
                .status(res.getStatus())
                .personId(res.getPerson().getId())
                .ownerId(res.getOwner().getId())
                .build();
    }

    public void deleteWorker(Long id) {
        var userId = userService.getCurrentUserId();
        if (workerRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Worker with id " + id + " not found");
        }
        if (!Objects.equals(userId, workerRepository.findById(id).get().getOwner().getId())) {
            throw new InsufficientEditingRightsException("You are not owner of this worker");
        }
        workerRepository.deleteById(id);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Worker.class.getSimpleName())
                        .type(ChangeType.DELETION)
                        .build().toString()
        );
    }

}
