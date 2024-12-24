package lab.arahnik.administration.service;

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
import java.util.Date;

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
}
