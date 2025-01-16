package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.response.OrganizationDto;
import lab.arahnik.manager.entity.Address;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.enums.ChangeType;
import lab.arahnik.manager.repository.AddressRepository;
import lab.arahnik.manager.repository.OrganizationRepository;
import lab.arahnik.websocket.handler.TextSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final TextSocketHandler textSocketHandler;
  private final UserService userService;
  private final AddressRepository addressRepository;
  private final UserRepository userRepository;
  private final Validator validator;

  public List<OrganizationDto> all() {
    var organizations = organizationRepository.findAll();
    return toDto(organizations);
  }

  public List<OrganizationDto> page(Pageable pageable) {
    var organizations = organizationRepository
            .findAll(pageable)
            .getContent();
    return toDto(organizations);
  }

  private List<OrganizationDto> toDto(List<Organization> organizations) {
    List<OrganizationDto> res = new ArrayList<>();
    for (var organization : organizations) {
      res.add(
              OrganizationDto
                      .builder()
                      .id(organization.getId())
                      .zipCode(organization
                              .getAddress()
                              .getZipCode())
                      .annualTurnover(organization.getAnnualTurnover())
                      .employeesCount(organization.getEmployeesCount())
                      .fullName(organization.getFullName())
                      .rating(organization.getRating())
                      .ownerId(organization
                              .getOwner()
                              .getId())
                      .isEditableByAdmin(organization.getEditableByAdmin())
                      .build()
      );
    }
    return res;
  }

  public OrganizationDto getById(Long id) {
    var organization = organizationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Organization with id " + id + " not found"));
    return OrganizationDto
            .builder()
            .id(organization.getId())
            .zipCode(organization
                    .getAddress()
                    .getZipCode())
            .annualTurnover(organization.getAnnualTurnover())
            .employeesCount(organization.getEmployeesCount())
            .fullName(organization.getFullName())
            .rating(organization.getRating())
            .ownerId(organization
                    .getOwner()
                    .getId())
            .build();
  }

  @Transactional
  public OrganizationDto create(Organization organization) {
    validateOrganization(organization);
    var res = organizationRepository.save(organization);
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(Organization.class.getSimpleName())
                    .type(ChangeType.CREATION)
                    .build()
                    .toString());
    return OrganizationDto
            .builder()
            .id(res.getId())
            .zipCode(res
                    .getAddress()
                    .getZipCode())
            .annualTurnover(res.getAnnualTurnover())
            .employeesCount(0L)
            .fullName(res.getFullName())
            .rating(res.getRating())
            .ownerId(res
                    .getOwner()
                    .getId())
            .build();
  }

  @Transactional
  public List<OrganizationDto> saveAll(List<Organization> organizations) {
    List<OrganizationDto> res = new ArrayList<>(organizations.size());
    for (var organization : organizations) {
      res.add(create(organization));
    }
    return res;
  }

  @Transactional
  public OrganizationDto update(OrganizationDto organizationDto) {
    var organization = organizationRepository
            .findById(organizationDto.getId())
            .orElseThrow(() -> new EntityNotFoundException(
                    "Organization with id " + organizationDto.getId() + " not found"));

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, organization);

    if (!organizationDto
            .getZipCode()
            .equals(organization
                    .getAddress()
                    .getZipCode())) {
      organization.setAddress(
              addressRepository.save(
                      Address
                              .builder()
                              .zipCode(organizationDto.getZipCode())
                              .build()
              )
      );
    }

    organization.setAnnualTurnover(organizationDto.getAnnualTurnover());
    organization.setFullName(organizationDto.getFullName());
    organization.setRating(organizationDto.getRating());

    validateOrganization(organization);

    var updatedOrganization = organizationRepository.save(organization);

    sendEvent(ChangeType.UPDATE, Organization.class.getSimpleName());

    return mapToOrganizationDto(updatedOrganization);
  }

  public void delete(Long id) {
    var organization = organizationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Organization with id " + id + " not found"));

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, organization);

    organizationRepository.deleteById(id);

    sendEvent(ChangeType.DELETION, Organization.class.getSimpleName());
  }

  private OrganizationDto mapToOrganizationDto(Organization organization) {
    return OrganizationDto
            .builder()
            .id(organization.getId())
            .zipCode(organization
                    .getAddress()
                    .getZipCode())
            .annualTurnover(organization.getAnnualTurnover())
            .employeesCount(organization.getEmployeesCount())
            .fullName(organization.getFullName())
            .rating(organization.getRating())
            .ownerId(organization
                    .getOwner()
                    .getId())
            .build();
  }

  private void validateEditingRights(User user, Organization organization) {
    if (!Objects.equals(user.getId(), organization
            .getOwner()
            .getId()) &&
            !(user.getRole() == Role.ADMIN && organization.getEditableByAdmin())) {
      throw new InsufficientEditingRightsException("You do not have permission to modify this organization");
    }
  }

  private void sendEvent(ChangeType changeType, String objectType) {
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(objectType)
                    .type(changeType)
                    .build()
                    .toString()
    );
  }

  private User getCurrentUserOrThrow() {
    var userId = userService.getCurrentUserId();
    return userRepository
            .findById(userId)
            .orElseThrow(
                    () -> new EntityNotFoundException("User not found")
            );
  }

  public void validateOrganization(Organization organization) {
    Set<ConstraintViolation<Organization>> violations = validator.validate(organization);

    if (!violations.isEmpty()) {
      StringBuilder message = new StringBuilder();
      for (ConstraintViolation<Organization> violation : violations) {
        message
                .append(violation.getMessage())
                .append("\n");
      }
      throw new RuntimeException(message.toString());
    }
  }

}
