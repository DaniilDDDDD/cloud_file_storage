package web.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.cloudfilestorage.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByFirstNameOrLastName(String firstName, String lastName);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserById(Long id);
    void deleteUserByUsername(String username);
}
