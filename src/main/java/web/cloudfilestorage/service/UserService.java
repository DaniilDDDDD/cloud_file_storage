package web.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import web.cloudfilestorage.dto.user.UserRegister;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.repository.RoleRepository;
import web.cloudfilestorage.repository.UserRepository;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// TODO: change updating User Entities - remove calling of setters from service (check if it's good solution)
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findById(
            Long id
    ) throws EntityNotFoundException {
        Optional<User> user = userRepository.findUserById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(
                    "User with id " + id + " is not present in database!"
            );
        }
        return user.get();
    }

    public User findByUsername(
            String username
    ) throws EntityNotFoundException {

        Optional<User> user = userRepository.findUserByUsername(username);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(
                    "User " + username + " is not present in database!"
            );
        }
        return user.get();
    }

    public User findByEmail(
            String email
    ) throws EntityNotFoundException {
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(
                    "User with email " + email + " is not present in database!"
            );
        }
        return user.get();
    }

    public User create(UserRegister userData) throws EntityExistsException {

        Optional<User> userByUsername = userRepository.findUserByUsername(userData.getUsername());
        Optional<User> userByEmail = userRepository.findUserByUsername(userData.getEmail());
        if (userByUsername.isPresent()) {
            throw new EntityExistsException("User with username " + userData.getUsername() + " already exists!");
        }
        if (userByEmail.isPresent()) {
            throw new EntityExistsException("User with email " + userData.getEmail() + " already exists!");
        }

        User user = new User();
        user.setUsername(userData.getUsername());
        user.setEmail(userData.getEmail());
        user.setPassword(passwordEncoder.encode(userData.getPassword()));
        user.setFirstName(userData.getFirstName());
        user.setLastName(userData.getLastName());
        Role role_user = roleRepository.findRoleByName("ROLE_USER").get();
        user.setRoles(List.of(role_user));
        user.setStatus(Status.ACTIVE);
        return userRepository.save(user);
    }


    public User update(UserUpdate userUpdate, Long id) {
        Optional<User> userData = userRepository.findUserById(id);
        if (userData.isEmpty()) {
            throw new EntityNotFoundException("User with id " + id + " is not present in the database");
        }

        User user = userData.get();
        user.setUsername(
                userUpdate.getUsername() != null ?
                        userUpdate.getUsername() : user.getUsername()
        );
        user.setFirstName(
                userUpdate.getFirstName() != null ?
                        userUpdate.getFirstName() : user.getFirstName()
        );
        user.setLastName(
                userUpdate.getLastName() != null ?
                        userUpdate.getLastName() : user.getLastName()
        );
        user.setPassword(
                userUpdate.getPassword() != null ?
                        passwordEncoder.encode(userUpdate.getPassword()) :
                        user.getPassword()
        );
        user.setStatus(
                userUpdate.getStatus() != null ?
                        userUpdate.getStatus() :
                        user.getStatus()
        );
        user.setRoles(
                userUpdate.getRoles() != null ?
                        userUpdate.getRoles() :
                        user.getRoles()
        );
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) throws EntityNotFoundException {

        Optional<User> user = userRepository.findUserById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(
                    "User with id " + id + " is not present in database!"
            );
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void delete(String username) throws EntityNotFoundException {
        Optional<User> user = userRepository.findUserByUsername(username);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(
                    "User with username " + username + " is not present in database!"
            );
        }
        userRepository.deleteUserByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User with username " + username + " is not present in database!");
        }
        return user.get();
    }

}
