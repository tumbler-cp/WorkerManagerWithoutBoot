package lab.arahnik.manager.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.response.PersonDto;
import lab.arahnik.manager.entity.Event;
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

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final TextSocketHandler textSocketHandler;

    public List<PersonDto> allPersons() {
        var persons = personRepository.findAll();
        return getPersonDtos(persons);

    }

    public List<PersonDto> allPersonsPage(Pageable pageable) {
        var persons = personRepository.findAll(pageable).getContent();
        return getPersonDtos(persons);
    }

    private List<PersonDto> getPersonDtos(List<Person> persons) {
        List<PersonDto> res = new ArrayList<>(persons.size());
        for (var person : persons) {
            res.add(
                    PersonDto.builder()
                            .id(person.getId())
                            .eyeColor(person.getEyeColor())
                            .hairColor(person.getHairColor())
                            .locationId(person.getLocation().getId())
                            .height(person.getHeight())
                            .weight(person.getWeight())
                            .passportID(person.getPassportID())
                            .ownerId(person.getOwner().getId())
                            .build()
            );
        }
        return res;
    }

    public PersonDto getPersonById(Long id) {
        var person = personRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Person not found")
        );
        return PersonDto.builder()
                .id(person.getId())
                .eyeColor(person.getEyeColor())
                .hairColor(person.getHairColor())
                .locationId(person.getLocation().getId())
                .height(person.getHeight())
                .weight(person.getWeight())
                .passportID(person.getPassportID())
                .ownerId(person.getOwner().getId())
                .build();
    }

    public PersonDto createPerson(Person person) {
        var res = personRepository.save(person);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Person.class.getSimpleName())
                        .type(ChangeType.CREATION)
                        .build().toString());
        return PersonDto.builder()
                .id(res.getId())
                .eyeColor(res.getEyeColor())
                .hairColor(res.getHairColor())
                .locationId(res.getLocation().getId())
                .height(res.getHeight())
                .weight(res.getWeight())
                .passportID(res.getPassportID())
                .ownerId(res.getOwner().getId())
                .build();
    }

    public PersonDto updatePerson(PersonDto personDto) {
        var person = personRepository.findById(personDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
        var userId = userService.getCurrentUserId();
        if (!Objects.equals(userId, person.getOwner().getId())) {
            throw new InsufficientEditingRightsException("You do not have permission to update this person");
        }
        person.setEyeColor(personDto.getEyeColor());
        person.setHairColor(personDto.getHairColor());
        person.setLocation(locationRepository.findById(personDto.getId()).orElseThrow(
                () -> new EntityNotFoundException("Location not found")
        ));
        person.setHeight(personDto.getHeight());
        person.setWeight(personDto.getWeight());
        person.setPassportID(personDto.getPassportID());
        var res = personRepository.save(person);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Person.class.getSimpleName())
                        .type(ChangeType.UPDATE)
                        .build().toString());
        return PersonDto.builder()
                .id(res.getId())
                .eyeColor(res.getEyeColor())
                .hairColor(res.getHairColor())
                .locationId(res.getLocation().getId())
                .height(res.getHeight())
                .weight(res.getWeight())
                .passportID(res.getPassportID())
                .ownerId(res.getOwner().getId())
                .build();
    }

    public void deletePerson(Long id) {
        var userId = userService.getCurrentUserId();
        if (personRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Person not found");
        }
        if (!Objects.equals(userId, personRepository.findById(id).get().getOwner().getId())) {
            throw new InsufficientEditingRightsException("You do not have permission to delete this person");
        }
        personRepository.deleteById(id);
        textSocketHandler.sendMessage(
                Event.builder()
                        .object(Person.class.getSimpleName())
                        .type(ChangeType.DELETION)
                        .build().toString());
    }

}
