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
import lab.arahnik.manager.dto.response.PersonDto;
import lab.arahnik.manager.entity.Event;
import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.entity.Person;
import lab.arahnik.manager.enums.ChangeType;
import lab.arahnik.manager.repository.LocationRepository;
import lab.arahnik.manager.repository.PersonRepository;
import lab.arahnik.websocket.handler.TextSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonRepository personRepository;
  private final UserService userService;
  private final LocationRepository locationRepository;
  private final TextSocketHandler textSocketHandler;
  private final UserRepository userRepository;
  private final Validator validator;

  public List<PersonDto> allPersons() {
    var persons = personRepository.findAll();
    return getPersonDtos(persons);

  }

  public List<PersonDto> allPersonsPage(Pageable pageable) {
    var persons = personRepository
            .findAll(pageable)
            .getContent();
    return getPersonDtos(persons);
  }

  private List<PersonDto> getPersonDtos(List<Person> persons) {
    List<PersonDto> res = new ArrayList<>(persons.size());
    for (var person : persons) {
      res.add(
              PersonDto
                      .builder()
                      .id(person.getId())
                      .eyeColor(person.getEyeColor())
                      .hairColor(person.getHairColor())
                      .locationId(person
                              .getLocation()
                              .getId())
                      .height(person.getHeight())
                      .weight(person.getWeight())
                      .passportID(person.getPassportID())
                      .ownerId(person
                              .getOwner()
                              .getId())
                      .isEditableByAdmin(person.getEditableByAdmin())
                      .build()
      );
    }
    return res;
  }

  public PersonDto getPersonById(Long id) {
    var person = personRepository
            .findById(id)
            .orElseThrow(
                    () -> new EntityNotFoundException("Person not found")
            );
    return PersonDto
            .builder()
            .id(person.getId())
            .eyeColor(person.getEyeColor())
            .hairColor(person.getHairColor())
            .locationId(person
                    .getLocation()
                    .getId())
            .height(person.getHeight())
            .weight(person.getWeight())
            .passportID(person.getPassportID())
            .ownerId(person
                    .getOwner()
                    .getId())
            .build();
  }

  public PersonDto createPerson(Person person) {
    validatePerson(person);
    var res = personRepository.save(person);
    textSocketHandler.sendMessage(
            Event
                    .builder()
                    .object(Person.class.getSimpleName())
                    .type(ChangeType.CREATION)
                    .build()
                    .toString());
    return PersonDto
            .builder()
            .id(res.getId())
            .eyeColor(res.getEyeColor())
            .hairColor(res.getHairColor())
            .locationId(res
                    .getLocation()
                    .getId())
            .height(res.getHeight())
            .weight(res.getWeight())
            .passportID(res.getPassportID())
            .ownerId(res
                    .getOwner()
                    .getId())
            .build();
  }

  public List<PersonDto> saveAllPersons(List<Person> persons) {
    List<PersonDto> res = new ArrayList<>(persons.size());
    for (var person : persons) {
      res.add(createPerson(person));
    }
    return res;
  }

  public PersonDto updatePerson(PersonDto personDto) {
    var person = personRepository
            .findById(personDto.getId())
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, person);

    person.setEyeColor(personDto.getEyeColor());
    person.setHairColor(personDto.getHairColor());
    person.setLocation(locationRepository
            .findById(personDto.getLocationId())
            .orElseThrow(
                    () -> new EntityNotFoundException("Location not found")
            ));
    person.setHeight(personDto.getHeight());
    person.setWeight(personDto.getWeight());
    person.setPassportID(personDto.getPassportID());

    validatePerson(person);

    var updatedPerson = personRepository.save(person);

    sendEvent(ChangeType.UPDATE, Person.class.getSimpleName());

    return mapToPersonDto(updatedPerson);
  }

  public void deletePerson(Long id) {
    var person = personRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    var user = getCurrentUserOrThrow();
    validateEditingRights(user, person);

    personRepository.deleteById(id);

    sendEvent(ChangeType.DELETION, Person.class.getSimpleName());
  }

  private PersonDto mapToPersonDto(Person person) {
    return PersonDto
            .builder()
            .id(person.getId())
            .eyeColor(person.getEyeColor())
            .hairColor(person.getHairColor())
            .locationId(person
                    .getLocation()
                    .getId())
            .height(person.getHeight())
            .weight(person.getWeight())
            .passportID(person.getPassportID())
            .ownerId(person
                    .getOwner()
                    .getId())
            .build();
  }

  private void validateEditingRights(User user, Person person) {
    if (!Objects.equals(user.getId(), person
            .getOwner()
            .getId()) &&
            !(user.getRole() == Role.ADMIN && person.getEditableByAdmin())) {
      throw new InsufficientEditingRightsException("You do not have permission to modify this person");
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

  public void validatePerson(Person person) {
    Set<ConstraintViolation<Person>> violations = validator.validate(person);

    if (!violations.isEmpty()) {
      StringBuilder message = new StringBuilder();
      for (ConstraintViolation<Person> violation : violations) {
        message
                .append(violation.getMessage())
                .append("\n");
      }
      throw new RuntimeException(message.toString());
    }
  }

}
