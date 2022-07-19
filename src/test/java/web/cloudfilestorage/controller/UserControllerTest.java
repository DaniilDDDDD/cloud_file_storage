package web.cloudfilestorage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import web.cloudfilestorage.dto.user.UserLogin;
import web.cloudfilestorage.dto.user.UserRegister;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.security.JwtTokenProvider;
import web.cloudfilestorage.service.UserService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    private final UserService userService = Mockito.mock(UserService.class);

    private final AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);

    private final JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    private final Role role_user = Role.builder().name("ROLE_USER").build();

    private User user_1;

    private User user_2;

    private final UserLogin userLogin1 = UserLogin.builder()
            .login(user_1.getUsername())
            .password(user_1.getPassword())
            .build();
    private final UserLogin userLogin2 = UserLogin.builder()
            .login(user_2.getUsername())
            .password(user_2.getPassword())
            .build();

    private final String user_1_token = "user_1_jwt_token";

    private final String user_2_token = "user_2_jwt_token";

    private final UserRegister newUserRegister = UserRegister.builder()
            .username("new_user")
            .email("new_user@test.com")
            .password("qwerty1234")
            .firstName("new_user_firstName")
            .lastName("new_user_lastName")
            .build();

    private final UserUpdate userUpdate = UserUpdate.builder()
            .username("user_updated")
            .password("qwerty1234")
            .firstName("user_updated_firstName")
            .lastName("user_updated_lastName")
            .status(Status.ACTIVE)
            .roles(List.of(role_user))
            .build();


    @BeforeEach
    void setUp() {

        user_1 = User.builder()
                .id(1L)
                .username("user_1")
                .email("user_1@test.com")
                .password("qwerty1234")
                .firstName("user_1_firstName")
                .lastName("user_1_lastName")
                .status(Status.ACTIVE)
                .roles(List.of(role_user))
                .build();

        user_2 = User.builder()
                .id(2L)
                .username("user_2")
                .email("user_2@test.com")
                .password("qwerty1234")
                .firstName("user_2_firstName")
                .lastName("user_2_lastName")
                .status(Status.ACTIVE)
                .roles(List.of(role_user))
                .build();

        Mockito.when(userService.loadUserByUsername(user_1.getUsername()))
                .thenReturn(user_1);
        Mockito.when(userService.loadUserByUsername(user_2.getUsername()))
                .thenReturn(user_2);

        Mockito.when(userService.findByUsername(user_1.getUsername()))
                .thenReturn(user_1);
        Mockito.when(userService.findByUsername(user_2.getUsername()))
                .thenReturn(user_2);

        Mockito.when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user_1.getUsername(),
                        user_1.getPassword())
        )).thenReturn(Mockito.any(Authentication.class));
        Mockito.when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user_2.getUsername(),
                        user_2.getPassword())
        )).thenReturn(Mockito.any(Authentication.class));

        Mockito.when(jwtTokenProvider.createToken(
                        userLogin1.getLogin(),
                        user_1.getAuthorities()))
                .thenReturn(user_1_token);
        Mockito.when(jwtTokenProvider.createToken(
                        userLogin2.getLogin(),
                        user_2.getAuthorities()))
                .thenReturn(user_2_token);

        Mockito.when(userService.create(newUserRegister)).thenReturn(
                User.builder()
                        .username(newUserRegister.getUsername())
                        .email(newUserRegister.getEmail())
                        .password(passwordEncoder.encode(newUserRegister.getPassword()))
                        .firstName(newUserRegister.getFirstName())
                        .lastName(newUserRegister.getLastName())
                        .status(Status.ACTIVE)
                        .roles(List.of(role_user))
                        .build()
        );

    }

    @Test
    void register() throws Exception {
        mockMvc.perform(
                        post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRegister))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username")
                        .value(newUserRegister.getUsername()))
                .andExpect(jsonPath("$.email")
                        .value(newUserRegister.getEmail()))
                .andExpect(jsonPath("$.firstName")
                        .value(newUserRegister.getFirstName()))
                .andExpect(jsonPath("$.lastName")
                        .value(newUserRegister.getLastName()));
    }

    @Test
    void login() throws Exception {

        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLogin1))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value(userLogin1.getLogin()))
                .andExpect(jsonPath("$.token")
                        .value(user_1_token));

        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLogin2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value(userLogin2.getLogin()))
                .andExpect(jsonPath("$.token")
                        .value(user_2_token));
    }

    @Test
    void retrieve() throws Exception {
        mockMvc.perform(
                get("").header("Authorization", user_1_token)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").isNumber()
        ).andExpect(
                jsonPath("$.username").value(user_1.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user_1.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(user_1.getEmail())
        ).andExpect(
                jsonPath("$.lastName").value(user_1.getLastName())
        ).andExpect(
                jsonPath("$.status").value(Status.ACTIVE)
        ).andExpect(
                jsonPath("$.files").isArray()
        ).andExpect(
                jsonPath("$.roles").value(List.of(role_user))
        );

        mockMvc.perform(
                get("").header("Authorization", user_2_token)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").isNumber()
        ).andExpect(
                jsonPath("$.username").value(user_2.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user_2.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(user_2.getEmail())
        ).andExpect(
                jsonPath("$.lastName").value(user_2.getLastName())
        ).andExpect(
                jsonPath("$.status").value(Status.ACTIVE)
        ).andExpect(
                jsonPath("$.files").isArray()
        ).andExpect(
                jsonPath("$.roles").value(List.of(role_user))
        );
    }

    @Test
    void update() throws Exception {
        mockMvc.perform(
                put("")
                        .header("Authorization", user_1_token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate))
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").isNumber()
        ).andExpect(
                jsonPath("$.username").value(userUpdate.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user_1.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(userUpdate.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(userUpdate.getLastName())
        ).andExpect(
                jsonPath("$.status").value(userUpdate.getStatus())
        ).andExpect(
                jsonPath("$.roles").value(userUpdate.getRoles())
        ).andExpect(
                jsonPath("$.files").value(user_1.getFiles())
        );

        mockMvc.perform(
                put("")
                        .header("Authorization", user_2_token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate))
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").isNumber()
        ).andExpect(
                jsonPath("$.username").value(userUpdate.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user_2.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(userUpdate.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(userUpdate.getLastName())
        ).andExpect(
                jsonPath("$.status").value(userUpdate.getStatus())
        ).andExpect(
                jsonPath("$.roles").value(userUpdate.getRoles())
        ).andExpect(
                jsonPath("$.files").value(user_2.getFiles())
        );
    }

    @Test
    void deleteTest() throws Exception {
        mockMvc.perform(
                delete("").header("Authorization", user_1_token)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("User " + user_1.getUsername() + "is deleted!")
        );

       mockMvc.perform(
                delete("").header("Authorization", user_2_token)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("User " + user_2.getUsername() + "is deleted!")
        );
    }

}
