package lab.arahnik.manager.service;

import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.manager.dto.response.WorkerDto;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;

    public Worker findById(Long id) {
        return workerRepository.findById(id).orElse(null);
    }

    public List<WorkerDto> findAll() {
        List<Worker> workers = workerRepository.findAll();
        List<WorkerDto> res = new ArrayList<>();
        for (Worker worker : workers) {
            res.add(WorkerDto
                    .builder()
                    .id(worker.getId())
                    .name(worker.getName())
                    .status(worker.getStatus())
                    .build());
        }
        return res;
    }

    public List<WorkerDto> findAll(Pageable pageable) {
        List<Worker> workers = workerRepository.findAll(pageable).getContent();
        List<WorkerDto> res = new ArrayList<>();
        for (Worker worker : workers) {
            res.add(WorkerDto
                    .builder()
                    .id(worker.getId())
                    .name(worker.getName())
                    .status(worker.getStatus())
                    .ownerId(worker.getOwner().getId())
                    .build());
        }
        return res;
    }

    public WorkerDto save(Worker entity) {
        var worker = workerRepository.save(entity);
        return WorkerDto.builder()
                .id(worker.getId())
                .name(worker.getName())
                .status(worker.getStatus())
                .ownerId(worker.getOwner().getId())
                .build();
    }

    public Worker update(Worker entity) throws Exception {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userRepository.findByUsername(username).isEmpty()) throw new Exception("Server error");
        User user = userRepository.findByUsername(username).get();
        if (!user.getId().equals(entity.getOwner().getId())) {
            throw new Exception("No privileges");
        }
        return workerRepository.save(entity);
    }

    public void delete(Long id) {
        workerRepository.deleteById(id);
    }
}
