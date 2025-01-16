package lab.arahnik.authentication.repository;

import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  List<User> findAllByRole(Role role);

}
