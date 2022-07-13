package web.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.cloudfilestorage.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleById(Long id);
    Optional<Role> findRoleByName(String name);

}