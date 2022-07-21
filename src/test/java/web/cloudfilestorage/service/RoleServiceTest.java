package web.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import web.cloudfilestorage.dto.role.RoleData;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.repository.RoleRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class RoleServiceTest {

    @MockBean
    private RoleRepository roleRepository;

    private RoleService roleService;

    private Role roleAdmin;

    private Role roleUser;

    private RoleData roleData;

    @BeforeEach
    void setUp() {

        roleService = new RoleService(roleRepository);

        roleAdmin = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .users(List.of())
                .build();

        roleUser = Role.builder()
                .id(2L)
                .name("ROLE_USER")
                .users(List.of())
                .build();

        roleData = RoleData.builder()
                .name("ROLE_NEW")
                .build();

        Mockito.when(roleRepository.findAll()).thenReturn(List.of(roleAdmin, roleUser));

        Mockito.when(roleRepository.findRoleById(1L)).thenReturn(Optional.of(roleAdmin));
        Mockito.when(roleRepository.findRoleById(2L)).thenReturn(Optional.of(roleUser));

        Mockito.when(roleRepository.findRoleByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));
        Mockito.when(roleRepository.findRoleByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        Mockito.when(roleRepository.findRoleByName("ROLE_UNPRESENT")).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findRoleById(3L)).thenReturn(Optional.empty());

        Mockito.when(roleRepository.save(Mockito.any(Role.class)))
                .then(AdditionalAnswers.returnsFirstArg());

    }

    @Test
    void read() {
        List<Role> roles = roleService.list();

        assertThat(roles).isNotEmpty();
        assertThat(roles.size() == 2).isTrue();
        assertThat(roles.get(0)).isEqualTo(roleAdmin);
        assertThat(roles.get(1)).isEqualTo(roleUser);

        Role admin = roleService.retrieve(roleAdmin.getName());
        assertThat(admin.getId()).isEqualTo(roleAdmin.getId());
        assertThat(admin.getName()).isEqualTo(roleAdmin.getName());
        assertThat(admin.getUsers()).isEqualTo(roleAdmin.getUsers());

        Role user = roleService.retrieve(roleUser.getName());
        assertThat(user.getId()).isEqualTo(roleUser.getId());
        assertThat(user.getName()).isEqualTo(roleUser.getName());
        assertThat(user.getUsers()).isEqualTo(roleUser.getUsers());

        assertThatThrownBy(
                () -> roleService.retrieve(3L)
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "Role with id " + 3L + " is not present in database!"
        );
        assertThatThrownBy(
                () -> roleService.retrieve("ROLE_UNPRESENT")
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "Role with name ROLE_UNPRESENT is not present in database!"
        );

    }

    @Test
    void create() {

        Role new_role = roleService.create(roleData);
        assertThat(new_role.getName()).isEqualTo(roleData.getName());
        assertThat(new_role.getUsers()).isEmpty();

    }

    @Test
    void update() {

        Role admin_updated = roleService.update(roleData, roleAdmin.getId());
        assertThat(admin_updated.getId()).isEqualTo(roleAdmin.getId());
        assertThat(admin_updated.getUsers()).isEqualTo(roleAdmin.getUsers());
        assertThat(admin_updated.getName()).isEqualTo(roleData.getName());

        Role user_updated = roleService.update(roleData, roleUser.getId());
        assertThat(user_updated.getId()).isEqualTo(roleUser.getId());
        assertThat(user_updated.getUsers()).isEqualTo(roleUser.getUsers());
        assertThat(user_updated.getName()).isEqualTo(roleUser.getName());


        assertThatThrownBy(
                () -> roleService.update(roleData, 3L)
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "Role with id " + 3L + " is not present in database!"
        );

        assertThatThrownBy(
                () -> roleService.update(roleData, "ROLE_UNPRESENT")
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "Role with name ROLE_UNPRESENT is not present in database!"
        );

    }

    @Test
    void delete() {

        assertThatNoException().isThrownBy(
                () -> roleService.delete(roleAdmin.getId())
        );
        assertThatNoException().isThrownBy(
                () -> roleService.delete(roleAdmin.getName())
        );

        assertThatThrownBy(
                () -> roleService.delete(3L)
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "Role with id " + 3L + " is not present in database!"
        );

        assertThatThrownBy(
                () -> roleService.delete("ROLE_UNPRESENT")
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "Role with name ROLE_UNPRESENT is not present in database!"
        );

    }


}
