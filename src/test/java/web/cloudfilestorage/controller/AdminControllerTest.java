package web.cloudfilestorage.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import web.cloudfilestorage.dto.file.FileData;
import web.cloudfilestorage.dto.file.FileView;
import web.cloudfilestorage.dto.role.RoleData;
import web.cloudfilestorage.dto.user.UserUpdateByAdmin;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.model.File;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.security.JwtTokenProvider;
import web.cloudfilestorage.service.FileService;
import web.cloudfilestorage.service.RoleService;
import web.cloudfilestorage.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static web.cloudfilestorage.utils.TestFIleUtil.getMultipartFile;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private FileService fileService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final Role roleAdmin = Role.builder()
            .id(1L)
            .name("ROLE_ADMIN")
            .build();

    private final Role roleUser = Role.builder()
            .id(2L)
            .name("ROLE_USER")
            .build();

    private User user;

    private final String userToken = "user_token";

    private Authentication userAuthentication;

    private User admin;

    private final String adminToken = "admin_token";

    private Authentication adminAuthentication;

    private File file1;

    private File file2;

    private final FileData fileMetadata = FileData.builder()
            .description("New description").build();

    private MockMultipartFile multipartFile;

    private final UserUpdateByAdmin userUpdate = UserUpdateByAdmin.builder()
            .username("user_updated")
            .password("qwerty1234")
            .firstName("user_updated_firstName")
            .lastName("user_updated_lastName")
            .status(Status.ACTIVE)
            .roles(List.of(roleUser))
            .build();

    private RoleData roleData;


    @BeforeEach
    void setUp() throws JwtAuthenticationException, IOException {

        admin = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@test.com")
                .password("qwerty1234")
                .firstName("admin_firstName")
                .lastName("admin_lastName")
                .status(Status.ACTIVE)
                .files(List.of())
                .roles(List.of(roleAdmin))
                .build();
        roleAdmin.setUsers(List.of(admin));

        user = User.builder()
                .id(2L)
                .username("user")
                .email("user@test.com")
                .password("qwerty1234")
                .firstName("user_firstName")
                .lastName("user_lastName")
                .status(Status.ACTIVE)
                .files(List.of())
                .roles(List.of(roleUser))
                .build();
        roleUser.setUsers(List.of(user));

        adminAuthentication = new UsernamePasswordAuthenticationToken(
                admin, "", admin.getAuthorities()
        );
        userAuthentication = new UsernamePasswordAuthenticationToken(
                user, "", user.getAuthorities()
        );

        Mockito.when(userService.loadUserByUsername(admin.getUsername()))
                .thenReturn(admin);
        Mockito.when(userService.loadUserByUsername(user.getUsername()))
                .thenReturn(user);
        Mockito.when(userService.findByUsername(admin.getUsername()))
                .thenReturn(admin);
        Mockito.when(userService.findByUsername(user.getUsername()))
                .thenReturn(user);

        Mockito.when(userService.update(userUpdate, user)).thenReturn(
                User.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(userUpdate.getUsername())
                        .firstName(userUpdate.getFirstName())
                        .lastName(userUpdate.getLastName())
                        .password(userUpdate.getPassword())
                        .roles(userUpdate.getRoles())
                        .status(userUpdate.getStatus())
                        .files(user.getFiles())
                        .build()
        );

        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user, admin));

        Mockito.when(jwtTokenProvider.getUsername(adminToken))
                .thenReturn(admin.getUsername());
        Mockito.when(jwtTokenProvider.validateToken(adminToken))
                .thenReturn(Boolean.TRUE);
        Mockito.when(jwtTokenProvider.getAuthentication(adminToken))
                .thenReturn(adminAuthentication);

        Mockito.when(jwtTokenProvider.getUsername(userToken))
                .thenReturn(user.getUsername());
        Mockito.when(jwtTokenProvider.validateToken(userToken))
                .thenReturn(Boolean.TRUE);
        Mockito.when(jwtTokenProvider.getAuthentication(userToken))
                .thenReturn(userAuthentication);

        Mockito.when(jwtTokenProvider.resolveToken(Mockito.any(HttpServletRequest.class)))
                .thenAnswer(
                        i -> {
                            HttpServletRequest request = (HttpServletRequest) i.getArguments()[0];
                            String token = request.getHeader("Authorization");
                            if (token != null && token.startsWith("Bearer_")) {
                                return token.substring(7);
                            }
                            return token;
                        }
                );

        file1 = File.builder()
                .id(1L)
                .owner(user)
                .file("/user_file.jpg")
                .description("user file")
                .uploadDate(LocalDateTime.now())
                .build();
        user.setFiles(List.of(file1));
        file2 = File.builder()
                .id(2L)
                .owner(admin)
                .file("/admin_file.jpg")
                .description("Admin file")
                .uploadDate(LocalDateTime.now())
                .build();
        admin.setFiles(List.of(file2));

        multipartFile = getMultipartFile("src/test/resources/test_files/test_image1.jpg", "file");

        Mockito.when(fileService.findAllFiles())
                .thenReturn(List.of(file1, file2));

        Mockito.when(fileService.fileById(file1.getId())).thenReturn(file1);
        Mockito.when(fileService.fileById(file2.getId())).thenReturn(file2);


        Mockito.when(fileService.update(
                file1, multipartFile, fileMetadata
        )).thenReturn(File.builder()
                .id(file1.getId())
                .file("updated_file1.some_type")
                .description(fileMetadata.getDescription())
                .owner(user)
                .uploadDate(file1.getUploadDate())
                .build());
        Mockito.when(fileService.update(
                file2, multipartFile, fileMetadata
        )).thenReturn(File.builder()
                .id(file2.getId())
                .file("updated_file2.some_type")
                .description(fileMetadata.getDescription())
                .owner(admin)
                .uploadDate(file2.getUploadDate())
                .build());

        Mockito.when(roleService.list()).thenReturn(List.of(roleUser, roleAdmin));

        Mockito.when(roleService.retrieve("ROLE_ADMIN")).thenReturn(roleAdmin);
        Mockito.when(roleService.retrieve("ROLE_USER")).thenReturn(roleUser);

        Mockito.when(roleService.retrieve(roleAdmin.getName())).thenReturn(roleAdmin);
        Mockito.when(roleService.retrieve(roleUser.getName())).thenReturn(roleUser);

        roleData = new RoleData();
        roleData.setName("ROLE_NEW");

        Mockito.when(roleService.create(roleData)).thenReturn(
                Role.builder()
                        .id(3L)
                        .name(roleData.getName())
                        .users(List.of())
                        .build()
        );

        Mockito.when(roleService.update(roleData, roleUser.getName()))
                .thenReturn(Role.builder()
                        .id(roleUser.getId())
                        .name(roleData.getName())
                        .build());
    }

    @Test
    void listUsersTest() throws Exception {

        mockMvc.perform(
                get("/api/admin/users")
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        MvcResult response = mockMvc.perform(
                get("/api/admin/users")
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andReturn();

        List<Map<String, Object>> responseBody = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                new TypeReference<List<Map<String, Object>>>() {
                });

        assertThat(responseBody.size()).isEqualTo(2);

        assertThat(responseBody.get(0).getOrDefault("id", null))
                .isNotEqualTo(responseBody.get(1).getOrDefault("id", null))
                .isIn(user.getId().intValue(), admin.getId().intValue());
        assertThat(responseBody.get(1).getOrDefault("id", null))
                .isIn(user.getId().intValue(), admin.getId().intValue());

        assertThat(
                responseBody.get(0).getOrDefault("username", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("username", null)
        ).isIn(user.getUsername(), admin.getUsername());
        assertThat(
                responseBody.get(1).getOrDefault("username", null)
        ).isIn(user.getUsername(), admin.getUsername());

        assertThat(
                responseBody.get(0).getOrDefault("firstName", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("firstName", null)
        ).isIn(user.getFirstName(), admin.getFirstName());
        assertThat(
                responseBody.get(1).getOrDefault("firstName", null)
        ).isIn(user.getFirstName(), admin.getFirstName());

        assertThat(
                responseBody.get(0).getOrDefault("lastName", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("lastName", null)
        ).isIn(user.getLastName(), admin.getLastName());
        assertThat(
                responseBody.get(1).getOrDefault("lastName", null)
        ).isIn(user.getLastName(), admin.getLastName());

        assertThat(
                responseBody.get(0).getOrDefault("email", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("email", null)
        ).isIn(user.getEmail(), admin.getEmail());
        assertThat(
                responseBody.get(1).getOrDefault("email", null)
        ).isIn(user.getEmail(), admin.getEmail());

        assertThat(
                responseBody.get(0).getOrDefault("files", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("files", null)
        );
        assertThat(
                responseBody.get(0).getOrDefault("files", null)
        ).isInstanceOf(List.class);
        assertThat(
                responseBody.get(1).getOrDefault("files", null)
        ).isInstanceOf(List.class);

        assertThat(
                responseBody.get(0).getOrDefault("status", null)
        ).isEqualTo(String.valueOf(Status.ACTIVE));
        assertThat(
                responseBody.get(1).getOrDefault("status", null)
        ).isEqualTo(String.valueOf(Status.ACTIVE));

        assertThat(
                responseBody.get(0).getOrDefault("roles", null)
        ).isNotEqualTo(responseBody.get(1).getOrDefault("roles", null));

    }

    @Test
    void retrieveUserTest() throws Exception {

        mockMvc.perform(
                get("/api/admin/users/{username}", user.getUsername())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                get("/api/admin/users/{username}", user.getUsername())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                get("/api/admin/users/{username}", user.getUsername())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(user.getId())
        ).andExpect(
                jsonPath("$.username").value(user.getUsername())
        ).andExpect(
                jsonPath("$.email").value(user.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(user.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(user.getLastName())
        ).andExpect(
                jsonPath("$.status").value(String.valueOf(Status.ACTIVE))
        ).andExpect(
                jsonPath("$.roles").isArray()
        ).andExpect(
                jsonPath("$.files").isArray()
        );

        mockMvc.perform(
                get("/api/admin/users/{username}", admin.getUsername())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(admin.getId())
        ).andExpect(
                jsonPath("$.username").value(admin.getUsername())
        ).andExpect(
                jsonPath("$.email").value(admin.getEmail())
        ).andExpect(
                jsonPath("$.firstName").value(admin.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(admin.getLastName())
        ).andExpect(
                jsonPath("$.status").value(String.valueOf(Status.ACTIVE))
        ).andExpect(
                jsonPath("$.roles").isArray()
        ).andExpect(
                jsonPath("$.files").isArray()
        );

    }

    @Test
    void updateUserTest() throws Exception {

        mockMvc.perform(
                put("/api/admin/users/{username}", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate))
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                put("/api/admin/users/{username}", user.getUsername())
                        .header("Authorization", "Bearer_" + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate))
        ).andExpect(
                status().isForbidden()
        );
         mockMvc.perform(
                put("/api/admin/users/{username}", admin.getUsername())
                        .header("Authorization", "Bearer_" + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate))
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                put("/api/admin/users/{username}", user.getUsername())
                        .header("Authorization", "Bearer_" + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate))
        ).andExpect(
                jsonPath("$.id").value(user.getId())
        ).andExpect(
                jsonPath("$.username").value(userUpdate.getUsername())
        ).andExpect(
                jsonPath("$.firstName").value(userUpdate.getFirstName())
        ).andExpect(
                jsonPath("$.lastName").value(userUpdate.getLastName())
        ).andExpect(
                jsonPath("$.roles").isArray()
        ).andExpect(
                jsonPath("$.files").isArray()
        ).andExpect(
                jsonPath("$.status").value(String.valueOf(userUpdate.getStatus()))
        );

    }

    @Test
    void deleteUserTest() throws Exception {

        mockMvc.perform(
                delete("/api/admin/users/{username}", user.getUsername())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                delete("/api/admin/users/{username}", user.getUsername())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                delete("/api/admin/users/{username}", admin.getUsername())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                delete("/api/admin/users/{username}", user.getUsername())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("User " + user.getUsername() + " is deleted!")
        );

    }

    @Test
    void listRolesTest() throws Exception {

        mockMvc.perform(
                get("/api/admin/roles")
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                get("/api/admin/roles")
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );
        MvcResult response = mockMvc.perform(
                get("/api/admin/roles")
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andReturn();

        List<Map<String, Object>> responseBody = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                new TypeReference<List<Map<String, Object>>>() {
                });

        assertThat(responseBody.size()).isEqualTo(2);

        assertThat(responseBody.get(0).getOrDefault("id", null))
                .isNotEqualTo(responseBody.get(1).getOrDefault("id", null))
                .isIn(roleAdmin.getId().intValue(), roleUser.getId().intValue());
        assertThat(responseBody.get(1).getOrDefault("id", null))
                .isIn(roleAdmin.getId().intValue(), roleUser.getId().intValue());

        assertThat(
                responseBody.get(0).getOrDefault("name", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("name", null)
        ).isIn(roleAdmin.getName(), roleUser.getName());
        assertThat(
                responseBody.get(1).getOrDefault("name", null)
        ).isIn(roleAdmin.getName(), roleUser.getName());

    }

    @Test
    void retrieveRoleTest() throws Exception {

        mockMvc.perform(
                get("/api/admin/roles/{name}", roleAdmin.getName())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                get("/api/admin/roles/{name}", roleAdmin.getName())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                get("/api/admin/roles/{name}", roleAdmin.getName())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(roleAdmin.getId())
        ).andExpect(
                jsonPath("$.name").value(roleAdmin.getName())
        );
        mockMvc.perform(
                get("/api/admin/roles/{name}", roleUser.getName())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(roleUser.getId())
        ).andExpect(
                jsonPath("$.name").value(roleUser.getName())
        );

    }

    @Test
    void createRoleTest() throws Exception {

        mockMvc.perform(
                post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleData))
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                post("/api/admin/roles")
                        .header("Authorization", "Bearer_" + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleData))
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                post("/api/admin/roles")
                        .header("Authorization", "Bearer_" + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleData))
        ).andExpect(
                status().isCreated()
        ).andExpect(
                jsonPath("$.id").value(3L)
        ).andExpect(
                jsonPath("$.name").value(roleData.getName())
        );

    }

    @Test
    void updateRoleTest() throws Exception {
        mockMvc.perform(
                put("/api/admin/roles/{name}", roleUser.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleData))
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                put("/api/admin/roles/{name}", roleUser.getName())
                        .header("Authorization", "Bearer_" + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleData))
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                put("/api/admin/roles/{name}", roleUser.getName())
                        .header("Authorization", "Bearer_" + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleData))
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(roleUser.getId())
        ).andExpect(
                jsonPath("$.name").value(roleData.getName())
        );
    }

    @Test
    void deleteRoleTest() throws Exception {

        mockMvc.perform(
                delete("/api/admin/roles/{name}", roleUser.getName())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                delete("/api/admin/roles/{name}", roleUser.getName())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                delete("/api/admin/roles/{name}", roleUser.getName())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("Role " + roleUser.getName() + " was deleted from database")
        );

    }

    @Test
    void listFilesTest() throws Exception {

        mockMvc.perform(
                get("/api/admin/files")
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                get("/api/admin/files")
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        MvcResult response = mockMvc.perform(
                get("/api/admin/files")
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isOk()
        ).andReturn();

        List<Map<String, Object>> responseBody = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                new TypeReference<List<Map<String, Object>>>() {
                });

        assertThat(responseBody.size()).isEqualTo(2);

        assertThat(responseBody.get(0).getOrDefault("id", null))
                .isNotEqualTo(responseBody.get(1).getOrDefault("id", null))
                .isIn(file1.getId().intValue(), file2.getId().intValue());
        assertThat(responseBody.get(1).getOrDefault("id", null))
                .isIn(file1.getId().intValue(), file2.getId().intValue());

        assertThat(responseBody.get(0).getOrDefault("file", null))
                .isNotEqualTo(responseBody.get(1).getOrDefault("file", null))
                .isIn(file1.getFile(), file2.getFile());
        assertThat(responseBody.get(1).getOrDefault("file", null))
                .isIn(file1.getFile(), file2.getFile());

        assertThat(
                responseBody.get(0).getOrDefault("description", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("description", null)
        ).isIn(file1.getDescription(), file2.getDescription());
        assertThat(
                responseBody.get(1).getOrDefault("description", null)
        ).isIn(file1.getDescription(), file2.getDescription());

    }

    @Test
    void retrieveFileTest() throws Exception {

        mockMvc.perform(
                get("/api/admin/files/{id}", file1.getId())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                get("/api/admin/files/{id}", file1.getId())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

         mockMvc.perform(
                 get("/api/admin/files/{id}", file1.getId())
                         .header("Authorization", "Bearer_" + adminToken)
         ).andExpect(
                 status().isOk()
         ).andExpect(
                 jsonPath("$.id").value(file1.getId())
         ).andExpect(
                 jsonPath("$.file").value(file1.getFile())
         ).andExpect(
                 jsonPath("$.description").value(file1.getDescription())
         ).andExpect(
                 jsonPath("$.owner").value(file1.getOwner().getId())
         ).andExpect(
                 jsonPath("$.uploadDate").value(file1.getUploadDate().toString())
         );

         mockMvc.perform(
                 get("/api/admin/files/{id}", file2.getId())
                         .header("Authorization", "Bearer_" + adminToken)
         ).andExpect(
                 status().isOk()
         ).andExpect(
                 jsonPath("$.id").value(file2.getId())
         ).andExpect(
                 jsonPath("$.file").value(file2.getFile())
         ).andExpect(
                 jsonPath("$.description").value(file2.getDescription())
         ).andExpect(
                 jsonPath("$.owner").value(file2.getOwner().getId())
         ).andExpect(
                 jsonPath("$.uploadDate").value(file2.getUploadDate().toString())
         );
    }

    @Test
    void updateFileTest() throws Exception {

        MockMultipartHttpServletRequestBuilder builderAccessDeniedFile1 =
                MockMvcRequestBuilders.multipart("/api/admin/files/{id}", file1.getId());
        builderAccessDeniedFile1.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        builderAccessDeniedFile1.param("description", fileMetadata.getDescription());

        mockMvc.perform(
                builderAccessDeniedFile1.file(multipartFile)
        ).andExpect(
                status().isForbidden()
        );

        builderAccessDeniedFile1.header("Authorization", "Bearer_" + userToken);
        mockMvc.perform(
                builderAccessDeniedFile1.file(multipartFile)
        ).andExpect(
                status().isForbidden()
        );

        MockMultipartHttpServletRequestBuilder builderAccessDeniedFile2 =
                MockMvcRequestBuilders.multipart("/api/admin/files/{id}", file2.getId());
        builderAccessDeniedFile2.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        builderAccessDeniedFile2.param("description", fileMetadata.getDescription());

        mockMvc.perform(
                builderAccessDeniedFile2.file(multipartFile)
        ).andExpect(
                status().isForbidden()
        );

        builderAccessDeniedFile2.header("Authorization", "Bearer_" + userToken);
        mockMvc.perform(
                builderAccessDeniedFile2.file(multipartFile)
        ).andExpect(
                status().isForbidden()
        );


        MockMultipartHttpServletRequestBuilder builderFile1 =
                MockMvcRequestBuilders.multipart("/api/admin/files/{id}", file1.getId());
        builderFile1.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        builderFile1.param("description", fileMetadata.getDescription());

        builderFile1.header("Authorization", "Bearer_" + adminToken);
        mockMvc.perform(
                builderFile1.file(multipartFile)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(file1.getId())
        ).andExpect(
                jsonPath("$.file").value("updated_file1.some_type")
        ).andExpect(
                jsonPath("$.description").value(fileMetadata.getDescription())
        );

        MockMultipartHttpServletRequestBuilder builderFile2 =
                MockMvcRequestBuilders.multipart("/api/admin/files/{id}", file2.getId());
        builderFile2.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        builderFile2.param("description", fileMetadata.getDescription());

        builderFile2.header("Authorization", "Bearer_" + adminToken);
        mockMvc.perform(
                builderFile2.file(multipartFile)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.id").value(file2.getId())
        ).andExpect(
                jsonPath("$.file").value("updated_file2.some_type")
        ).andExpect(
                jsonPath("$.description").value(fileMetadata.getDescription())
        );

    }

    @Test
    void deleteFileTest() throws Exception {
        mockMvc.perform(
                delete("/api/admin/files/{id}", file1.getId())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                delete("/api/admin/files/{id}", file1.getId())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                delete("/api/admin/files/{id}", file2.getId())
        ).andExpect(
                status().isForbidden()
        );
        mockMvc.perform(
                delete("/api/admin/files/{id}", file2.getId())
                        .header("Authorization", "Bearer_" + userToken)
        ).andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                delete("/api/admin/files/{id}", file1.getId())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("File with id " + file1.getId() + " is deleted")
        );


        mockMvc.perform(
                delete("/api/admin/files/{id}", file2.getId())
                        .header("Authorization", "Bearer_" + adminToken)
        ).andExpect(
                status().isNoContent()
        ).andExpect(
                content().string("File with id " + file2.getId() + " is deleted")
        );

    }

}
