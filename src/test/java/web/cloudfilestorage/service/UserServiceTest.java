package web.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import web.cloudfilestorage.dto.user.UserRegister;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.repository.RoleRepository;
import web.cloudfilestorage.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class UserServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);

    private final BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

    private UserService userService;

    private final Role role_user = Role.builder().name("ROLE_USER").build();
    private final Role role_admin = Role.builder().name("ROLE_ADMIN").build();

    private User user;

    @BeforeEach
    void setUp() {

        userService = new UserService(userRepository, roleRepository, passwordEncoder);

        user = User.builder()
                .id(1L)
                .username("User_1")
                .email("user_1@test.com")
                .password("qwerty1234")
                .firstName("user_1_firstName")
                .lastName("user_1_lastName")
                .status(Status.DISABLED)
                .roles(List.of(role_user))
                .build();

        Mockito.when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findUserByUsername("User_1")).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findUserByEmail("user_1@test.com")).thenReturn(Optional.of(user));

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Mockito.when(userRepository.findById(4L)).thenReturn(Optional.empty());
        Mockito.when(userRepository.findUserByUsername("NotPresentUser")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findUserByUsername("NotPresentUser@test.com")).thenReturn(Optional.empty());

        Mockito.when(roleRepository.findRoleByName("ROLE_ADMIN")).thenReturn(Optional.ofNullable(role_admin));
        Mockito.when(roleRepository.findRoleByName("ROLE_USER")).thenReturn(Optional.ofNullable(role_user));
    }
    @Test
    void read() {
        assertThatNoException().isThrownBy(
                () -> userService.findById(1L)
        );
        assertThatNoException().isThrownBy(
                () -> userService.findByUsername("User_1")
        );
        assertThatNoException().isThrownBy(
                () -> userService.findByEmail("user_1@test.com")
        );

        assertThatThrownBy(
                () -> userService.findById(4L)
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "User with id " + 4L + " is not present in database!"
        );
        assertThatThrownBy(
                () -> userService.findByUsername("NotPresentUser")
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "User NotPresentUser is not present in database!"
        );
        assertThatThrownBy(
                () -> userService.findByEmail("NotPresentUser@test.com")
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "User with email NotPresentUser@test.com is not present in database!"
        );
    }

    @Test
    void create() {
        UserRegister userRegister = UserRegister.builder()
                .username("User_2")
                .email("user_2@test.com")
                .password("qwerty1234")
                .firstName("user_2_firstName")
                .lastName("user_2_lastName")
                .build();

        User registeredUser = userService.create(userRegister);
        assertThat(registeredUser.getUsername()).isEqualTo(userRegister.getUsername());
        assertThat(registeredUser.getFirstName()).isEqualTo(userRegister.getFirstName());
        assertThat(registeredUser.getLastName()).isEqualTo(userRegister.getLastName());
        assertThat(registeredUser.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(registeredUser.getRoles()).isEqualTo(List.of(role_user));
        assertThat(
                passwordEncoder.encode(registeredUser.getPassword())
        ).isEqualTo(
                passwordEncoder.encode(userRegister.getPassword())
        );
    }

    @Test
    void update() {

        UserUpdate userUpdate = UserUpdate.builder()
                .username("User_1_updated")
                .password("1234qwerty")
                .firstName("user_1_firstName_updated")
                .lastName("user_1_lastName_updated")
                .build();

        User userUpdated = userService.update(userUpdate, user.getId());
        assertThat(userUpdated.getUsername()).isEqualTo(userUpdate.getUsername());
        assertThat(userUpdated.getFirstName()).isEqualTo(userUpdate.getFirstName());
        assertThat(userUpdated.getLastName()).isEqualTo(userUpdate.getLastName());
        assertThat(userUpdated.getStatus()).isEqualTo(user.getStatus());
        assertThat(userUpdated.getRoles()).isEqualTo(user.getRoles());
        assertThat(
                passwordEncoder.encode(userUpdated.getPassword())
        ).isEqualTo(
                passwordEncoder.encode(userUpdate.getPassword())
        );

    }

    @Test
    void delete() {

        assertThatNoException().isThrownBy(
                () -> userService.delete(1L)
        );
        assertThatNoException().isThrownBy(
                () -> userService.delete("User_1")
        );

        assertThatThrownBy(
                () -> userService.delete("NotPresentUser")
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "User with username NotPresentUser is not present in database!"
        );

        assertThatThrownBy(
                () -> userService.delete(4L)
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "User with id " + 4L + " is not present in database!"
        );
    }

    @Test
    @DisplayName("Present user must be loaded by username from database.")
    void loadUserByUsername() {

        User loadedUser = (User) userService.loadUserByUsername(user.getUsername());

        assertThat(loadedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(loadedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(loadedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(loadedUser.getStatus()).isEqualTo(user.getStatus());
        assertThat(loadedUser.getRoles()).isEqualTo(user.getRoles());
        assertThat(
                passwordEncoder.encode(loadedUser.getPassword())
        ).isEqualTo(
                passwordEncoder.encode(user.getPassword())
        );

        assertThatThrownBy(
                () -> userService.loadUserByUsername("NotPresentUser")
        ).isInstanceOf(UsernameNotFoundException.class).hasMessage(
                "User with username NotPresentUser is not present in database!"
        );

    }
}
