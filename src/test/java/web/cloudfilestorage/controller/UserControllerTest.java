package web.cloudfilestorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import web.cloudfilestorage.dto.user.UserLogin;
import web.cloudfilestorage.dto.user.UserRegister;
import web.cloudfilestorage.dto.user.UserUpdate;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.security.JwtTokenProvider;
import web.cloudfilestorage.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final Role role_user = Role.builder()
            .id(1L)
            .name("ROLE_USER")
            .build();

    private User user_1;

    private User user_2;

    private UserLogin userLogin1;

    private UserLogin userLogin2;

    private Authentication authenticationUser1;

    private Authentication authenticationUser2;

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
            .build();


    @BeforeEach
    void setUp() throws JwtAuthenticationException {

        user_1 = User.builder()
                .id(1L)
                .username("user_1")
                .email("user_1@test.com")
                .password("qwerty1234")
                .firstName("user_1_firstName")
                .lastName("user_1_lastName")
                .status(Status.ACTIVE)
                .files(List.of())
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
                .files(List.of())
                .roles(List.of(role_user))
                .build();

        userLogin1 = UserLogin.builder()
                .login(user_1.getUsername())
                .password(user_1.getPassword())
                .build();
        userLogin2 = UserLogin.builder()
                .login(user_2.getUsername())
                .password(user_2.getPassword())
                .build();


        Mockito.when(userService.loadUserByUsername(user_1.getUsername()))
                .thenReturn(user_1);
        Mockito.when(userService.loadUserByUsername(user_2.getUsername()))
                .thenReturn(user_2);

        Mockito.when(userService.findByUsername(user_1.getUsername()))
                .thenReturn(user_1);
        Mockito.when(userService.findByUsername(user_2.getUsername()))
                .thenReturn(user_2);

        Mockito.when(userService.create(newUserRegister)).thenReturn(
                User.builder()
                        .id(3L)
                        .username(newUserRegister.getUsername())
                        .email(newUserRegister.getEmail())
                        .password(newUserRegister.getPassword() + "_encoded")
                        .firstName(newUserRegister.getFirstName())
                        .lastName(newUserRegister.getLastName())
                        .status(Status.ACTIVE)
                        .roles(List.of(role_user))
                        .build()
        );

        Mockito.when(userService.update(userUpdate, user_1.getId()))
                .thenReturn(
                        User.builder()
                                .id(user_1.getId())
                                .username(userUpdate.getUsername())
                                .email(user_1.getEmail())
                                .firstName(userUpdate.getFirstName())
                                .lastName(userUpdate.getLastName())
                                .password(userUpdate.getPassword())
                                .status(user_1.getStatus())
                                .roles(user_1.getRoles())
                                .build()
                );
        Mockito.when(userService.update(userUpdate, user_2.getId()))
                .thenReturn(
                        User.builder()
                                .id(user_2.getId())
                                .username(userUpdate.getUsername())
                                .email(user_2.getEmail())
                                .firstName(userUpdate.getFirstName())
                                .lastName(userUpdate.getLastName())
                                .password(userUpdate.getPassword())
                                .status(user_2.getStatus())
                                .roles(user_2.getRoles())
                                .build()
                );

        authenticationUser1 = new UsernamePasswordAuthenticationToken(
                user_1, "", user_1.getAuthorities()
        );
        authenticationUser2 = new UsernamePasswordAuthenticationToken(
                user_2, "", user_2.getAuthorities()
        );

        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(null);

        Mockito.when(jwtTokenProvider.createToken(
                        userLogin1.getLogin(),
                        user_1.getAuthorities()))
                .thenReturn(user_1_token);
        Mockito.when(jwtTokenProvider.createToken(
                        userLogin2.getLogin(),
                        user_2.getAuthorities()))
                .thenReturn(user_2_token);

        Mockito.when(jwtTokenProvider.getUsername(user_1_token))
                .thenReturn(user_1.getUsername());
        Mockito.when(jwtTokenProvider.getUsername(user_2_token))
                .thenReturn(user_2.getUsername());

        Mockito.when(jwtTokenProvider.validateToken(user_1_token))
                .thenReturn(Boolean.TRUE);
        Mockito.when(jwtTokenProvider.validateToken(user_2_token))
                .thenReturn(Boolean.TRUE);

        Mockito.when(jwtTokenProvider.getAuthentication(user_1_token))
                .thenReturn(authenticationUser1);
        Mockito.when(jwtTokenProvider.getAuthentication(user_2_token))
                .thenReturn(authenticationUser2);


        Mockito.when(jwtTokenProvider.resolveToken(Mockito.any(HttpServletRequest.class)))
                .thenAnswer(
                        i -> {
                            HttpServletRequest request = (HttpServletRequest) i.getArguments()[0];
                            String token = request.getHeader("Authorization");
                            if (token != null && token.startsWith("Bearer_")) {
                                return token.substring(7);
                            }
                            ;
                            return token;
                        }
                );

    }

    @Test
    void registerTest() throws Exception {
        mockMvc.perform(
                        post("/api/users/register")
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
    void loginTest() throws Exception {

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLogin1))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login")
                        .value(userLogin1.getLogin()))
                .andExpect(jsonPath("$.token")
                        .value(user_1_token));

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLogin2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login")
                        .value(userLogin2.getLogin()))
                .andExpect(jsonPath("$.token")
                        .value(user_2_token));
    }

    @Test
    void retrieveTest() throws Exception {

        mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer_" + user_1_token)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").isNumber()
        ).andExpect(
                jsonPath("$.username").value(user_1.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user_1.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(user_1.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(user_1.getLastName())
        ).andExpect(
                jsonPath("$.status").value(String.valueOf(user_1.getStatus()))
        ).andExpect(
                jsonPath("$.files").isArray()
        ).andExpect(
                jsonPath("$.roles").isArray()
        );

        mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer_" + user_2_token)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").isNumber()
        ).andExpect(
                jsonPath("$.username").value(user_2.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user_2.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(user_2.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(user_2.getLastName())
        ).andExpect(
                jsonPath("$.status").value(String.valueOf(user_2.getStatus()))
        ).andExpect(
                jsonPath("$.files").isArray()
        ).andExpect(
                jsonPath("$.roles").isArray()
        );
    }

    @Test
    void updateTest() throws Exception {

        mockMvc.perform(
                put("/api/users")
                        .header("Authorization", "Bearer_" + user_1_token)
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
                jsonPath("$.status").value(String.valueOf(user_1.getStatus()))
        ).andExpect(
                jsonPath("$.roles").isArray()
        ).andExpect(
                jsonPath("$.files").isEmpty()
        );

        mockMvc.perform(
                put("/api/users")
                        .header("Authorization", "Bearer_" + user_2_token)
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
                jsonPath("$.status").value(String.valueOf(user_2.getStatus()))
        ).andExpect(
                jsonPath("$.roles").isArray()
        ).andExpect(
                jsonPath("$.files").isEmpty()
        );
    }

    @Test
    void deleteTest() throws Exception {
        mockMvc.perform(
                delete("/api/users").header("Authorization", "Bearer_" + user_1_token)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("User " + user_1.getUsername() + "is deleted!")
        );

        mockMvc.perform(
                delete("/api/users").header("Authorization", "Bearer_" + user_2_token)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("User " + user_2.getUsername() + "is deleted!")
        );
    }

}
