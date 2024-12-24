package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.response.OrganizationDto;
import lab.arahnik.manager.entity.Address;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.enums.EventType;
import lab.arahnik.manager.repository.AddressRepository;
import lab.arahnik.manager.repository.OrganizationRepository;
import lab.arahnik.websocket.handler.TextSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final TextSocketHandler textSocketHandler;
    private final UserService userService;
    private final AddressRepository addressRepository;

    public List<OrganizationDto> allOrganizations() {
        var organizations = organizationRepository.findAll();
        List<OrganizationDto> res = new ArrayList<>();
        for (var organization : organizations) {
            res.add(
                    OrganizationDto.builder()
                            .id(organization.getId())
                            .zipCode(organization.getAddress().getZipCode())
                            .annualTurnover(organization.getAnnualTurnover())
                            .employeeCount(organization.getEmployeesCount())
                            .fullName(organization.getFullName())
                            .rating(organization.getRating())
                            .ownerId(organization.getOwner().getId())
                            .build()
            );
        }
        return res;
    }

    public OrganizationDto getOrganizationById(Long id) {
        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization with id " + id + " not found"));
        return OrganizationDto.builder()
                .id(organization.getId())
                .zipCode(organization.getAddress().getZipCode())
                .annualTurnover(organization.getAnnualTurnover())
                .employeeCount(organization.getEmployeesCount())
                .fullName(organization.getFullName())
                .rating(organization.getRating())
                .ownerId(organization.getOwner().getId())
                .build();
    }

    public OrganizationDto createOrganization(Organization organization) {
        var res = organizationRepository.save(organization);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Organization.class.getSimpleName())
                        .type(EventType.CREATION)
                        .build().toString());
        return OrganizationDto.builder()
                .id(res.getId())
                .zipCode(res.getAddress().getZipCode())
                .annualTurnover(res.getAnnualTurnover())
                .employeeCount(res.getEmployeesCount())
                .fullName(res.getFullName())
                .rating(res.getRating())
                .ownerId(res.getOwner().getId())
                .build();
    }

    public OrganizationDto updateOrganization(OrganizationDto organizationDto) {
        var organization = organizationRepository.findById(organizationDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organization with id " + organizationDto.getId() + " not found"));
        var userId = userService.getCurrentUserId();
        if (!Objects.equals(userId, organization.getOwner().getId())) {
            throw new InsufficientEditingRightsException("You do not have permission to update this organization");
        }
        if (!organizationDto.getZipCode().equals(organization.getAddress().getZipCode())) {
            organization.setAddress(
                    addressRepository.save(
                            Address.builder().zipCode(organizationDto.getZipCode()).build()
                    )
            );
        }
        organization.setAnnualTurnover(organizationDto.getAnnualTurnover());
        organization.setEmployeesCount(organizationDto.getEmployeeCount());
        organization.setFullName(organizationDto.getFullName());
        organization.setRating(organizationDto.getRating());
        var res = organizationRepository.save(organization);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Organization.class.getSimpleName())
                        .type(EventType.UPDATE)
                        .build().toString()
        );
        return OrganizationDto.builder()
                .id(res.getId())
                .zipCode(res.getAddress().getZipCode())
                .annualTurnover(res.getAnnualTurnover())
                .employeeCount(res.getEmployeesCount())
                .fullName(res.getFullName())
                .rating(res.getRating())
                .ownerId(res.getOwner().getId())
                .build();
    }

    public void deleteOrganization(Long id) {
        var userId = userService.getCurrentUserId();
        if (organizationRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Organization with id " + id + " not found");
        }
        if (!Objects.equals(userId, organizationRepository.findById(id).get().getOwner().getId())) {
            throw new InsufficientEditingRightsException("You do not have permission to delete this organization");
        }
        organizationRepository.deleteById(id);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Organization.class.getSimpleName())
                        .type(EventType.DELETION)
                        .build().toString()
        );
    }
}
