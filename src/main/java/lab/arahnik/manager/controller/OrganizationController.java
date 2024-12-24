package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewOrganization;
import lab.arahnik.manager.dto.response.OrganizationDto;
import lab.arahnik.manager.entity.Address;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.repository.AddressRepository;
import lab.arahnik.manager.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserService userService;
    private final AddressRepository addressRepository;

    @GetMapping("/all")
    public List<OrganizationDto> allOrganizations() {
        return organizationService.allOrganizations();
    }

    @GetMapping
    public OrganizationDto findOrganizationById(@RequestParam("id") Long id) {
        return organizationService.getOrganizationById(id);
    }

    @PostMapping("/new")
    public OrganizationDto createOrganization(@RequestBody NewOrganization newOrganization) {
        var user = userService.getByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        return organizationService.createOrganization(
                Organization.builder()
                        .address(
                                addressRepository.save(
                                        Address.builder().zipCode(newOrganization.getZipCode()).build()
                                )
                        )
                        .annualTurnover(newOrganization.getAnnualTurnover())
                        .employeesCount(newOrganization.getEmployeesCount())
                        .fullName(newOrganization.getFullName())
                        .rating(newOrganization.getRating())
                        .owner(user)
                        .build()
        );
    }

    @PutMapping("/update")
    public OrganizationDto updateOrganization(@RequestBody OrganizationDto organizationDto) {
        return organizationService.updateOrganization(organizationDto);
    }

    @DeleteMapping("/delete")
    public void deleteOrganization(@RequestParam("id") Long id) {
        organizationService.deleteOrganization(id);
    }

    @ExceptionHandler({InsufficientEditingRightsException.class, EntityNotFoundException.class})
    public ResponseEntity<String> handleException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
