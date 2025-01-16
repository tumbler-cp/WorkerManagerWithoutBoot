package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.service.UserService;
import lab.arahnik.exception.InsufficientEditingRightsException;
import lab.arahnik.manager.dto.request.NewPerson;
import lab.arahnik.manager.dto.response.PersonDto;
import lab.arahnik.manager.entity.Person;
import lab.arahnik.manager.importer.service.FileLogService;
import lab.arahnik.manager.importer.service.PersonImportService;
import lab.arahnik.manager.repository.LocationRepository;
import lab.arahnik.manager.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

  private final PersonService personService;
  private final UserService userService;
  private final LocationRepository locationRepository;
  private final PersonImportService personImportService;
  private final FileLogService fileLogService;

  @GetMapping("/all")
  public List<PersonDto> allPersons() {
    return personService.allPersons();
  }

  @GetMapping("/find")
  public PersonDto getPersonById(@RequestParam("id") Long id) {
    return personService.getPersonById(id);
  }


  @GetMapping("/paged")
  public List<PersonDto> getPersonsByPage(
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
          @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
  ) {
    Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
    return personService.allPersonsPage(pageable);
  }

  @PostMapping("/new")
  public PersonDto createPerson(@RequestBody NewPerson newPerson) {
    var user = userService.getByUsername(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName()
    );
    return personService.createPerson(
            Person
                    .builder()
                    .eyeColor(newPerson.getEyeColor())
                    .hairColor(newPerson.getHairColor())
                    .location(locationRepository
                            .findById(newPerson.getLocationId())
                            .orElseThrow(
                                    () -> new EntityNotFoundException("Location not found")
                            ))
                    .height(newPerson.getHeight())
                    .weight(newPerson.getWeight())
                    .passportID(newPerson.getPassportID())
                    .owner(user)
                    .editableByAdmin(newPerson.getEditableByAdmin())
                    .build()
    );
  }

  @PostMapping("/upload")
  public List<PersonDto> uploadLocation(@RequestParam("file") MultipartFile file) throws IOException {
    String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
    file.transferTo(new File(tempFilePath));
    var res = personImportService.importPersons(tempFilePath);
    fileLogService.save(file.getOriginalFilename(), res.size(), Person.class.getSimpleName());
    return res;
  }

  @PutMapping("/update")
  public PersonDto updatePerson(@RequestBody PersonDto personDto) {
    return personService.updatePerson(personDto);
  }

  @DeleteMapping("/delete")
  public void deletePerson(@RequestParam(name = "id") Long id) {
    personService.deletePerson(id);
  }

  @ExceptionHandler({InsufficientEditingRightsException.class, EntityNotFoundException.class})
  public ResponseEntity<String> handleException(Exception exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

}
