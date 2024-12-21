package lab.arahnik.manager.controller;

import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.manager.dto.response.WorkerDto;
import lab.arahnik.manager.dto.request.WorkerRequestDto;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.enums.EventType;
import lab.arahnik.manager.service.WorkerService;
import lab.arahnik.websocket.handler.TextSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
public class WorkerController {
    private final WorkerService workerService;
    private final UserRepository userRepository;
    private final TextSocketHandler textSocketHandler;

    @GetMapping("/all")
    public List<WorkerDto> getAllWorkers() {
        return workerService.findAll();
    }

    @GetMapping("/paged")
    public List<WorkerDto> getWorkers(
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("sortDir") String sortDir,
            @RequestParam("sortParam") String sortParam
    ) {
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortParam);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return workerService.findAll(pageable);
    }

    @PostMapping("/new")
    public WorkerDto createWorker(@RequestBody WorkerRequestDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user;
        if (userRepository.findByUsername(username).isPresent()) {
            user = userRepository.findByUsername(username).get();
        } else throw new UsernameNotFoundException("Username not found");

        textSocketHandler.sendMessage(
                Event
                        .builder()
                        .type(EventType.CREATION)
                        .object(Worker.class.getName())
                        .build()
                        .toString());

        return workerService.save(
                Worker.builder()
                        .name(dto.getName())
                        .status(dto.getStatus())
                        .owner(user)
                        .build()
        );
    }
}
