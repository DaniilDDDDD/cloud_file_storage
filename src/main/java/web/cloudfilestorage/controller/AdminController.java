package web.cloudfilestorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.cloudfilestorage.dto.file.FileData;
import web.cloudfilestorage.dto.file.FileView;
import web.cloudfilestorage.dto.role.RoleData;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.model.File;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.service.FileService;
import web.cloudfilestorage.service.RoleService;
import web.cloudfilestorage.service.UserService;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

// TODO: add documentation
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrators' operations")
public class AdminController {

    private final UserService userService;

    private final RoleService roleService;

    private final FileService fileService;

    @Autowired
    public AdminController(
            UserService userService,
            RoleService roleService,
            FileService fileService) {
        this.userService = userService;
        this.roleService = roleService;
        this.fileService = fileService;
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/users")
    @Operation(
            summary = "List users",
            description = "List of all registered users"
    )
    public ResponseEntity<List<User>> listUsers(
    ) {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/users/{username}")
    @Operation(
            summary = "Retrieve user",
            description = "Retrieve information about concrete user"
    )
    public ResponseEntity<User> retrieveUser(
            @PathVariable(value = "username")
            @NotNull(message = "username must be provided as path variable")
            String username
    ) throws EntityNotFoundException {
        User user = userService.findByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/users/{username}")
    @Operation(
            summary = "Update user",
            description = "Update information about concrete user"
    )
    public ResponseEntity<User> updateUser(
            @PathVariable(value = "username")
            @NotNull(message = "username must be provided as path variable")
            String username,
            @Valid @RequestBody
            UserUpdate userUpdate
    ) throws EntityNotFoundException {

        User user = userService.findByUsername(username);

        return new ResponseEntity<>(
                userService.update(userUpdate, user.getId()),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/users/{username}")
    @Operation(
            summary = "Delete user",
            description = "Delete concrete user"
    )
    public ResponseEntity<String> deleteUser(
            @PathVariable(value = "username")
            @NotNull(message = "username must be provided as path variable")
            String username
    ) throws EntityNotFoundException {

        userService.delete(username);

        return new ResponseEntity<>(
                "User " + username + "is deleted!",
                HttpStatus.NO_CONTENT
        );

    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/roles")
    @Operation(
            summary = "List roles",
            description = "List all roles used by app"
    )
    public ResponseEntity<List<Role>> listRoles() {
        return new ResponseEntity<>(
                roleService.list(),
                HttpStatus.OK
        );
    }


    @Secured("ROLE_ADMIN")
    @GetMapping("/roles/{id}")
    @Operation(
            summary = "Retrieve role",
            description = "Retrieve by id information about concrete role"
    )
    public ResponseEntity<Role> retrieveRole(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided as path variable")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id
    ) throws EntityNotFoundException {
        return new ResponseEntity<>(
                roleService.retrieve(id),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/roles/{name}")
    @Operation(
            summary = "Retrieve role",
            description = "Retrieve by name information about concrete role by name"
    )
    public ResponseEntity<Role> retrieveRole(
            @PathVariable(value = "name")
            @NotNull(message = "name must be provided as path variable")
            String name
    ) throws EntityNotFoundException {
        return new ResponseEntity<>(
                roleService.retrieve(name),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/roles")
    @Operation(
            summary = "Create role",
            description = "Create role with provided name"
    )
    public ResponseEntity<Role> createRole(
            @Valid @RequestBody
            RoleData roleData
    ) {
        return new ResponseEntity<>(
                roleService.create(roleData),
                HttpStatus.OK
        );
    }

    @Secured("ROlE_ADMIN")
    @PutMapping("/roles/{id}")
    @Operation(
            summary = "Update role",
            description = "Update information about concrete role"
    )
    public ResponseEntity<Role> updateRole(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id,
            @RequestBody
            RoleData roleUpdate
    ) throws EntityNotFoundException {
        return new ResponseEntity<>(
                roleService.update(roleUpdate, id),
                HttpStatus.OK
        );
    }

    @Secured("ROlE_ADMIN")
    @PutMapping("/roles/{name}")
    @Operation(
            summary = "Update role",
            description = "Update information about concrete role"
    )
    public ResponseEntity<Role> updateRole(
            @PathVariable(value = "name")
            @NotNull(message = "name must be provided")
            String name,
            @RequestBody
            RoleData roleUpdate
    ) throws EntityNotFoundException {
        return new ResponseEntity<>(
                roleService.update(roleUpdate, name),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/roles/{id}")
    @Operation(
            summary = "Delete role",
            description = "Delete about concrete role"
    )
    public ResponseEntity<String> deleteRole(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id
    ) throws EntityNotFoundException {
        roleService.delete(id);
        return new ResponseEntity<>(
                "Role with id " + id + " was deleted from database",
                HttpStatus.NO_CONTENT
        );
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/roles/{name}")
    @Operation(
            summary = "Delete role",
            description = "Delete information about concrete role"
    )
    public ResponseEntity<String> deleteRole(
            @PathVariable(value = "name")
            @NotNull(message = "id must be provided")
            String name
    ) throws EntityNotFoundException {
        roleService.delete(name);
        return new ResponseEntity<>(
                "Role with name " + name + " was deleted from database",
                HttpStatus.NO_CONTENT
        );
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/files")
    @Operation(
            summary = "List files",
            description = "List all users of all owners"
    )
    public ResponseEntity<List<FileView>> listFiles() {
        List<FileView> files = fileService.findAllFiles().stream().map(
                FileView::new
        ).toList();
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/files/{id}")
    @Operation(
            summary = "Retrieve file",
            description = "Retrieve file of any user"
    )
    public ResponseEntity<FileView> retrieveFile(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id
    ) throws EntityNotFoundException {
        return new ResponseEntity<>(
                new FileView(
                        fileService.fileById(id)
                ),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/files/{id}")
    @Operation(
            summary = "Update file",
            description = "Update file of any user"
    )
    public ResponseEntity<FileView> updateFile(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id,
            @RequestParam(value = "file")
            MultipartFile multipartFile,
            FileData fileMetadata
    )
            throws
            IOException,
            EntityNotFoundException {

        File file = fileService.fileById(id);

        return new ResponseEntity<>(
                new FileView(
                        fileService.update(
                        file,
                        multipartFile,
                        fileMetadata
                    )
                ),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/files/{id}")
    @Operation(
            summary = "Delete file",
            description = "Delete file of any user"
    )
    public void deleteFile(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id
    )
            throws
            IOException,
            EntityNotFoundException
    {
        fileService.delete(fileService.fileById(id));
    }



}
