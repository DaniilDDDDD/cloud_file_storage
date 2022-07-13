package web.cloudfilestorage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.service.UserService;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers (
    ) {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/users/{username}")
    public ResponseEntity<User> retrieveUser (
            @PathVariable(value = "username")
            @NotNull(message = "username must be provided as path variable")
                    String username
    ) throws EntityNotFoundException {
        User user = userService.findUserByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/users/{username}")
    public ResponseEntity<User> update (
            @PathVariable(value = "username")
            @NotNull(message = "username must be provided as path variable")
                    String username,
            @Valid @RequestBody
                    UserUpdate userUpdate
    ) throws EntityNotFoundException {

        User user = userService.findUserByUsername(username);

        return new ResponseEntity<>(
                userService.update(userUpdate, user.getId()),
                HttpStatus.OK
        );
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/users/{username}")
    public ResponseEntity<String> delete (
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

}
