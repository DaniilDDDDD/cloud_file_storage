package web.cloudfilestorage.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.cloudfilestorage.dto.role.RoleUpdate;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.repository.RoleRepository;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(
            RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> list() {
        return roleRepository.findAll();
    }

    public Role retrieve(Long id) throws EntityNotFoundException {
        Optional<Role> role = roleRepository.findRoleById(id);
        if (role.isEmpty()) {
            throw new EntityNotFoundException(
                    "Role with id " + id + " is not present in database!"
            );
        }
        return role.get();
    }

    public Role retrieve(String name) throws EntityNotFoundException {
        Optional<Role> role = roleRepository.findRoleByName(name);
        if (role.isEmpty()) {
            throw new EntityNotFoundException(
                    "Role with name " + name + " is not present in database!"
            );
        }
        return role.get();
    }

    public Role update(
            RoleUpdate roleUpdate,
            Long id
    ) throws EntityNotFoundException {
        Optional<Role> roleData = roleRepository.findRoleById(id);
        if (roleData.isEmpty()) {
            throw new EntityNotFoundException(
                    "Role with id " + id + " is not present in database!"
            );
        }

        Role role = roleData.get();
        role.setName(
                roleUpdate.getName() != null ?
                        roleUpdate.getName() :
                        role.getName()
        );
        return roleRepository.save(role);
    }

    public Role update(
            RoleUpdate roleUpdate,
            String name
    ) throws EntityNotFoundException {
        Optional<Role> roleData = roleRepository.findRoleByName(name);
        if (roleData.isEmpty()) {
            throw new EntityNotFoundException(
                    "Role with name " + name + " is not present in database!"
            );
        }

        Role role = roleData.get();
        role.setName(
                roleUpdate.getName() != null ?
                        roleUpdate.getName() :
                        role.getName()
        );
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) throws EntityNotFoundException {
        Optional<Role> role = roleRepository.findRoleById(id);
        if (role.isEmpty()) {
            throw new EntityNotFoundException(
                    "Role with id " + id + " is not present in database!"
            );
        }
        roleRepository.delete(role.get());
    }

    @Transactional
    public void delete(String name) throws EntityNotFoundException {
        Optional<Role> role = roleRepository.findRoleByName(name);
        if (role.isEmpty()) {
            throw new EntityNotFoundException(
                    "Role with name " + name + " is not present in database!"
            );
        }
        roleRepository.delete(role.get());
    }

}
