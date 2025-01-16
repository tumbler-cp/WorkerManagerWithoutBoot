package lab.arahnik.manager.importer.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lab.arahnik.manager.importer.model.FileLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileLogRepository extends JpaRepository<FileLog, Long> {
  List<FileLog> findByOwner_Username(@NotNull @NotBlank @Size(min = 3, max = 64) String ownerUsername);
}
