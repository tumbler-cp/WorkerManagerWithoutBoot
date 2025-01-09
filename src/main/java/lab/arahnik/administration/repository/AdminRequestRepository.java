package lab.arahnik.administration.repository;

import jakarta.validation.constraints.NotNull;
import lab.arahnik.administration.entity.AdminRequest;
import lab.arahnik.administration.entity.AdminRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long> {

  List<AdminRequest> findAllByStatus(@NotNull AdminRequestStatus status);

  Optional<AdminRequest> findByUserId(Long userId);

}
