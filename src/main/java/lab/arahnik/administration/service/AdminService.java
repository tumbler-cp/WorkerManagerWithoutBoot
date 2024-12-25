package lab.arahnik.administration.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.administration.dto.AdminRequestDto;
import lab.arahnik.administration.dto.AdminRequestElem;
import lab.arahnik.administration.entity.AdminRequest;
import lab.arahnik.administration.entity.AdminRequestStatus;
import lab.arahnik.administration.repository.AdminRequestRepository;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRequestRepository adminRequestRepository;
    private final UserRepository userRepository;

    public List<AdminRequestElem> getAdminRequest() {
        var requests = adminRequestRepository.findAllByStatus(AdminRequestStatus.PENDING);
        List<AdminRequestElem> adminRequestElems = new ArrayList<>();
        for (var request : requests) {
            adminRequestElems.add(
                    AdminRequestElem.builder()
                            .id(request.getId())
                            .userId(request.getUser().getId())
                            .status(request.getStatus())
                            .build()
            );
        }
        return adminRequestElems;
    }

    public AdminRequestElem addAdminRequest(AdminRequestDto adminRequestDto) {
        var request = adminRequestRepository.save(
                AdminRequest.builder()
                        .user(userRepository.findByUsername(adminRequestDto.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(adminRequestDto.getUsername())))
                        .status(AdminRequestStatus.PENDING)
                        .build()
        );
        return  AdminRequestElem.builder()
                .userId(request.getUser().getId())
                .status(request.getStatus())
                .build();
    }

    public AdminRequestElem updateAdminRequest(AdminRequestElem adminRequestElem) {
        var request = adminRequestRepository.findByUserId(adminRequestElem.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with id " + adminRequestElem.getUserId() + " not found"));
        request.setStatus(adminRequestElem.getStatus());
        var res = adminRequestRepository.save(request);
        if (request.getStatus() == AdminRequestStatus.ACCEPTED) {
            var user = userRepository.findByUsername(request.getUser().getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(request.getUser().getUsername()));
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }
        return AdminRequestElem.builder()
                .id(res.getId())
                .userId(res.getUser().getId())
                .status(res.getStatus())
                .build();
    }
}
