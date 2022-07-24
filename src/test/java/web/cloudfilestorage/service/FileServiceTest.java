package web.cloudfilestorage.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;
import web.cloudfilestorage.dto.file.FileData;
import web.cloudfilestorage.model.File;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.repository.FileRepository;
import web.cloudfilestorage.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static web.cloudfilestorage.utils.TestFIleUtil.getMultipartFile;

public class FileServiceTest {

    private final String testFilesRoot = "src/test/resources/test_files/";

    private final FileRepository fileRepository = Mockito.mock(FileRepository.class);

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private FileService fileService;

    private MultipartFile multipartFile_1;

    private MultipartFile multipartFile_2;

    private static File file_1;

    private static File file_2;

    private final Role role_user = Role.builder().name("ROLE_USER").build();

    private final User owner_1 = User.builder()
            .id(1L)
            .username("owner_1")
            .email("owner_1@test.com")
            .password("qwerty1234")
            .firstName("owner_1_firstName")
            .lastName("owner_1_lastName")
            .status(Status.ACTIVE)
            .roles(List.of(role_user))
            .build();
    private final User owner_2 = User.builder()
            .id(2L)
            .username("owner_2")
            .email("owner_2@test.com")
            .password("qwerty1234")
            .firstName("owner_2_firstName")
            .lastName("owner_2_lastName")
            .status(Status.ACTIVE)
            .roles(List.of(role_user))
            .build();

    private Path owner_1FilesDirectory;
    private Path owner_2FilesDirectory;


    @BeforeEach
    void setUp() throws IOException {

        fileService = new FileService(
                userRepository,
                fileRepository
        );
        fileService.setFilesRoot(
                Path.of(testFilesRoot).getParent()
                        .resolve("media/files") + "/"
        );

        owner_1FilesDirectory = Path.of(testFilesRoot).getParent()
                .resolve("media/files/")
                .resolve(Path.of(String.valueOf(owner_1.getId())));
        owner_2FilesDirectory = Path.of(testFilesRoot).getParent()
                .resolve("media/files/")
                .resolve(Path.of(String.valueOf(owner_2.getId())));


        multipartFile_1 = getMultipartFile(testFilesRoot + "test_image1.jpg");
        multipartFile_2 = getMultipartFile(testFilesRoot + "test_image2.jpg");

        if (!Files.exists(owner_1FilesDirectory)) {
            Files.createDirectories(owner_1FilesDirectory);
        }
        if (!Files.exists(owner_2FilesDirectory)) {
            Files.createDirectories(owner_2FilesDirectory);
        }
        Files.copy(
                Path.of(testFilesRoot + "test_image1.jpg"),
                owner_1FilesDirectory.resolve("test_image1.jpg")
        );
        Files.copy(
                Path.of(testFilesRoot + "test_image2.jpg"),
                owner_2FilesDirectory.resolve("test_image2.jpg")
        );


        file_1 = File.builder()
                .id(1L)
                .file(
                        String.valueOf(owner_1FilesDirectory.resolve(multipartFile_1.getOriginalFilename()))
                )
                .description("Test file of owner 1")
                .owner(owner_1)
                .uploadDate(LocalDateTime.now())
                .build();

        file_2 = File.builder()
                .id(2L)
                .file(
                        String.valueOf(owner_2FilesDirectory.resolve(multipartFile_2.getOriginalFilename()))
                )
                .description("Test file of owner 2")
                .owner(owner_2)
                .uploadDate(LocalDateTime.now())
                .build();

        Mockito.when(fileRepository.save(Mockito.any(File.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Mockito.when(fileRepository.findAll()).thenReturn(List.of(file_1, file_2));

        Mockito.when(fileRepository.findFileById(file_1.getId()))
                .thenReturn(Optional.of(file_1));
        Mockito.when(fileRepository.findFileByFile(file_1.getFile()))
                .thenReturn(Optional.of(file_1));

        Mockito.when(fileRepository.findFileById(file_2.getId()))
                .thenReturn(Optional.of(file_2));
        Mockito.when(fileRepository.findFileByFile(file_2.getFile()))
                .thenReturn(Optional.of(file_2));

        Mockito.when(fileRepository.findAllByOwner_Username(owner_1.getUsername()))
                .thenReturn(List.of(file_1));
        Mockito.when(fileRepository.findAllByOwner_Username(owner_2.getUsername()))
                .thenReturn(List.of(file_2));

        Mockito.when(fileRepository.findFileById(3L))
                .thenReturn(Optional.empty());
        Mockito.when(fileRepository.findFileByFile("NotPresentFileName"))
                .thenReturn(Optional.empty());

        Mockito.when(userRepository.findUserByUsername(owner_1.getUsername()))
                .thenReturn(Optional.of(owner_1));
        Mockito.when(userRepository.findUserByUsername(owner_2.getUsername()))
                .thenReturn(Optional.of(owner_2));
        Mockito.when(userRepository.findUserByUsername("NotPresentUser"))
                .thenReturn(Optional.empty());

    }

    @Test
    void read() {

        List<File> files = fileService.findAllFiles();
        assertThat(files.size() == 2).isTrue();
        assertThat(files.get(0)).isIn(List.of(file_1, file_2));
        assertThat(files.get(1)).isIn(List.of(file_1, file_2));

        File foundFile_1 = fileService.fileById(file_1.getId());
        assertThat(foundFile_1).isEqualTo(file_1);
        File foundFile_2 = fileService.fileById(file_2.getId());
        assertThat(foundFile_2).isEqualTo(file_2);

        List<File> owner_1Files = fileService.allOwnerFiles(owner_1.getUsername());
        assertThat(owner_1Files.size() == 1).isTrue();
        assertThat(owner_1Files.get(0)).isEqualTo(file_1);
        List<File> owner_2Files = fileService.allOwnerFiles(owner_2.getUsername());
        assertThat(owner_2Files.size() == 1).isTrue();
        assertThat(owner_2Files.get(0)).isEqualTo(file_2);

    }

    @Test
    void create() throws IOException {

        FileData fileData = FileData.builder()
                .description("New file of owner 2")
                .build();
        File file = fileService.create(
                owner_2.getUsername(),
                multipartFile_1,
                fileData
        );

        assertThat(file).isInstanceOf(File.class);
        assertThat(file.getFile())
                .isEqualTo(
                        String.valueOf(owner_2FilesDirectory.resolve(multipartFile_1.getOriginalFilename()))
                );
        assertThat(file.getOwner()).isEqualTo(owner_2);
        assertThat(file.getDescription()).isEqualTo(fileData.getDescription());

        assertThatThrownBy(
                () -> fileService.create(
                        "NotPresentUser",
                        multipartFile_1,
                        fileData
                )
        ).isInstanceOf(EntityNotFoundException.class).hasMessage(
                "User NotPresentUser is not present in database!"
        );

    }

    @Test
    void update() throws IOException {

        FileData fileData = FileData.builder().description("File 1 updated").build();
        File file_updated = fileService.update(
                file_1,
                multipartFile_2,
                fileData
        );

        assertThat(file_updated.getFile())
                .isEqualTo(
                        String.valueOf(
                                owner_1FilesDirectory
                                        .resolve(multipartFile_2.getOriginalFilename()))
                );
        assertThat(
                Files.exists(
                        owner_1FilesDirectory
                                .resolve(multipartFile_2.getOriginalFilename())
                )
        ).isTrue();
        assertThat(file_updated.getId()).isEqualTo(file_1.getId());
        assertThat(file_updated.getDescription()).isEqualTo(fileData.getDescription());
        assertThat(file_updated.getOwner()).isEqualTo(file_1.getOwner());

    }

    @Test
    void delete() throws IOException {

        fileService.delete(file_1);
        assertThat(
                Files.exists(
                        owner_1FilesDirectory
                                .resolve(multipartFile_1.getOriginalFilename())
                )
        ).isFalse();

    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(
                new java.io.File(
                        String.valueOf(
                                Path.of(testFilesRoot).getParent().resolve("media")
                        )
                )
        );

    }

}
