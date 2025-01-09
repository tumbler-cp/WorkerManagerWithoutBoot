package lab.arahnik.manager.repository;

import lab.arahnik.manager.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

  @NonNull
  Page<Location> findAll(@NonNull Pageable pageable);

}
