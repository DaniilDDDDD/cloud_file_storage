package web.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.cloudfilestorage.model.File;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findFileById(Long id);

    Optional<File> findFileByShareLink(String shareLink);

    Optional<File> findFileByFile(String file);

    List<File> findAllByOwner_Username(String username);
}
