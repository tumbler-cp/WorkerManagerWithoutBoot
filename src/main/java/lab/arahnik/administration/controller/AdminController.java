package lab.arahnik.administration.controller;

import lab.arahnik.administration.dto.AdminRequestDto;
import lab.arahnik.administration.dto.AdminRequestElem;
import lab.arahnik.administration.dto.LogDto;
import lab.arahnik.administration.service.AdminService;
import lab.arahnik.administration.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final LogService logService;

    @PostMapping("/request")
    public AdminRequestElem makeAdminRequest() {
        AdminRequestDto adminRequestDto = AdminRequestDto.builder()
                .username(SecurityContextHolder.getContext().getAuthentication().getName())
                .build();
        return adminService.addAdminRequest(adminRequestDto);
    }

    @GetMapping("/reqlist")
    public List<AdminRequestElem> getRequests() {
        return adminService.getAdminRequest();
    }

    @GetMapping("/logs")
    public List<LogDto> allLogs() {
        return logService.allLogs();
    }

    @PutMapping("/update")
    public AdminRequestElem updateAdminRequest(@RequestBody AdminRequestElem adminRequestElem) {
        return adminService.updateAdminRequest(adminRequestElem);
    }
}
