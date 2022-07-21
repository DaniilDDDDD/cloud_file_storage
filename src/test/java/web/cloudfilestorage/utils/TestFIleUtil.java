package web.cloudfilestorage.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFIleUtil {

    public static MockMultipartFile getMultipartFile(String pathToFile) {
        Path path = Paths.get(pathToFile);
        String name = String.valueOf(path.getFileName());
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
        }
        return new MockMultipartFile(name,
                name, null, content);
    }

}
