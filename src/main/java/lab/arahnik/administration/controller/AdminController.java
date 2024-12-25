package lab.arahnik.administration.controller;

import lab.arahnik.administration.dto.AdminRequestDto;
import lab.arahnik.administration.dto.AdminRequestElem;
import lab.arahnik.administration.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/request")
    public AdminRequestElem makeAdminRequest(@RequestBody AdminRequestDto adminRequestDto) {
        return adminService.addAdminRequest(adminRequestDto);
    }

    @GetMapping("/reqlist")
    public List<AdminRequestElem> getRequests() {
        return adminService.getAdminRequest();
    }

    @PutMapping("/update")
    public AdminRequestElem updateAdminRequest(@RequestBody AdminRequestElem adminRequestElem) {
        return adminService.updateAdminRequest(adminRequestElem);
    }
}
