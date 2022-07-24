package web.cloudfilestorage.controller;


import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import web.cloudfilestorage.dto.user.UserLogin;
import web.cloudfilestorage.dto.user.UserRegister;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.security.JwtTokenProvider;
import web.cloudfilestorage.service.UserService;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;


@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Users' operations")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register",
            description = "Register user"
    )
    public ResponseEntity<User> register(
            @Valid @RequestBody UserRegister userRegister
    ) throws EntityExistsException {
        return new ResponseEntity<>(
                userService.create(userRegister),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Login user"
    )
    public ResponseEntity<UserLogin> login(
            @Validated({UserLogin.OnRequest.class})
            @RequestBody
            UserLogin userLogin
    )
            throws
            JwtException,
            AuthenticationException
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLogin.getLogin(),
                        userLogin.getPassword()
                )
        );
        UserDetails user = userService.loadUserByUsername(
                userLogin.getLogin()
        );

        String token = jwtTokenProvider.createToken(
                userLogin.getLogin(),
                user.getAuthorities()
        );

        UserLogin response = UserLogin.builder()
                .login(userLogin.getLogin()).token(token)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    @Operation(
            summary = "Retrieve user",
            description = "Retrieve information about authenticated user"
    )
    public ResponseEntity<User> retrieve(
            Authentication authentication
    ) throws JwtAuthenticationException {
        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }
        return ResponseEntity.ok(
                userService.findByUsername(authentication.getName())
        );
    }

    @PutMapping("")
    @Operation(
            summary = "Update user",
            description = "Update information about authenticated user"
    )
    public ResponseEntity<User> update(
            @Valid
            @RequestBody
            UserUpdate userUpdate,
            Authentication authentication
    ) throws JwtAuthenticationException {
        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }

        User principal = userService.findByUsername(authentication.getName());

        return ResponseEntity.ok(
                userService.update(userUpdate, principal.getId())
        );
    }

    @DeleteMapping("")
    @Operation(
            summary = "Delete user",
            description = "Delete authenticated user"
    )
    public ResponseEntity<String> delete(
            Authentication authentication
    ) throws JwtAuthenticationException {
        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }
        userService.delete(authentication.getName());
        return new ResponseEntity<>(
                "User " + authentication.getName() + "is deleted!",
                HttpStatus.NO_CONTENT
        );
    }

}
