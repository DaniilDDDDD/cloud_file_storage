package web.cloudfilestorage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;
import web.cloudfilestorage.dto.file.FileData;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.model.File;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.security.JwtTokenProvider;
import web.cloudfilestorage.service.FileService;
import web.cloudfilestorage.service.UserService;
import web.cloudfilestorage.utils.TestFIleUtil;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.*;
import static web.cloudfilestorage.utils.TestFIleUtil.getMultipartFile;


@WebMvcTest(FileController.class)
public class FileControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private FileService fileService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final Role role_user = Role.builder()
            .id(1L)
            .name("ROLE_USER")
            .build();

    private User owner;

    private final String ownerToken = "owner_token";

    private Authentication ownerAuthentication;

    private File file1;

    private File file2;

    private final FileData fileMetadata = FileData.builder()
            .description("New description").build();

    private MockMultipartFile multipartFile;

    @BeforeEach
    void setUp() throws JwtAuthenticationException {

        owner = User.builder()
                .id(1L)
                .username("owner")
                .email("owner@test.com")
                .password("qwerty1234")
                .firstName("owner_firstName")
                .lastName("owner_lastName")
                .status(Status.ACTIVE)
                .files(List.of())
                .roles(List.of(role_user))
                .build();

        ownerAuthentication = new UsernamePasswordAuthenticationToken(
                owner, "", owner.getAuthorities()
        );

        Mockito.when(userService.loadUserByUsername(owner.getUsername()))
                .thenReturn(owner);
        Mockito.when(userService.findByUsername(owner.getUsername()))
                .thenReturn(owner);

        Mockito.when(jwtTokenProvider.getUsername(ownerToken))
                .thenReturn(owner.getUsername());
        Mockito.when(jwtTokenProvider.validateToken(ownerToken))
                .thenReturn(Boolean.TRUE);
        Mockito.when(jwtTokenProvider.getAuthentication(ownerToken))
                .thenReturn(ownerAuthentication);
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

        file1 = File.builder()
                .id(1L)
                .owner(owner)
                .file("/owner_file_1.jpg")
                .description("Owner file 1")
                .uploadDate(LocalDateTime.now())
                .build();
        file2 = File.builder()
                .id(2L)
                .owner(owner)
                .file("/owner_file_2.jpg")
                .description("Owner file 2")
                .uploadDate(LocalDateTime.now())
                .build();

        Mockito.when(fileService.allOwnerFiles(owner.getUsername()))
                .thenReturn(List.of(file1, file2));


        multipartFile = getMultipartFile("src/test/resources/test_files/test_image1.jpg");


    }

    @Test
    void listTest() throws Exception {

        MvcResult response = mockMvc.perform(
                get("/api/files")
                        .header("Authorization", "Bearer_" + ownerToken)
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
                .isIn(1, 2);
        assertThat(responseBody.get(1).getOrDefault("id", null))
                .isIn(1, 2);

        assertThat(responseBody.get(0).getOrDefault("file", null))
                .isNotEqualTo(responseBody.get(1).getOrDefault("file", null))
                .isIn("/owner_file_1.jpg", "/owner_file_2.jpg");
        assertThat(responseBody.get(1).getOrDefault("file", null))
                .isIn("/owner_file_1.jpg", "/owner_file_2.jpg");

        assertThat(
                responseBody.get(0).getOrDefault("description", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("description", null)
        ).isIn("Owner file 1", "Owner file 2");
        assertThat(
                responseBody.get(1).getOrDefault("description", null)
        ).isIn("Owner file 1", "Owner file 2");

        assertThat(
                responseBody.get(0).getOrDefault("description", null)
        ).isNotEqualTo(
                responseBody.get(1).getOrDefault("description", null)
        ).isIn("Owner file 1", "Owner file 2");
        assertThat(
                responseBody.get(1).getOrDefault("description", null)
        ).isIn("Owner file 1", "Owner file 2");

    }


    @Test
    void createTest() throws Exception {
        mockMvc.perform(
                multipart("/api/files").file(multipartFile)
                        .header("Authorization", "Bearer_" + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileMetadata))
        ).andExpect(
                status().isCreated()
        );
    }
}
