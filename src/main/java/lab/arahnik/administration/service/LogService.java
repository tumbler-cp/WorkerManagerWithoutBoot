package lab.arahnik.administration.service;

import lab.arahnik.administration.dto.LogDto;
import lab.arahnik.administration.entity.Log;
import lab.arahnik.administration.repository.LogRepository;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.enums.ChangeType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final UserService userService;

    public void addLog(ChangeType type, Worker worker) {
        var user = userService.getByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        logRepository.save(
                Log.builder()
                        .user(user)
                        .worker(worker)
                        .time(LocalDateTime.now())
                        .changeType(type)
                        .build()
        );
    }

    public List<LogDto> allLogs() {
        var logs = logRepository.findAll();
        List<LogDto> logDtos = new ArrayList<>();
        for (var log : logs) {
            logDtos.add(
                    LogDto
                            .builder()
                            .id(log.getId())
                            .username(log.getUser().getUsername())
                            .userId(log.getUser().getId())
                            .workerName(log.getWorker().getName())
                            .workerId(log.getWorker().getId())
                            .changeType(log.getChangeType())
                            .time(log.getTime())
                            .build()
            );
        }
        return logDtos;
    }
}
