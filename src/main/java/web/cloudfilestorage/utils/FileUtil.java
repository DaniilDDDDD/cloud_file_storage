package web.cloudfilestorage.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtil {
    public static void saveFile(
            String uploadDir,
            String fileName,
            MultipartFile multipartFile
    ) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + fileName, ioe);
        }
    }

    public static void updateFile(
            String currentPathToFile,
            String newFileName,
            MultipartFile multipartFile
    ) throws IOException {
        Path currentPath = Paths.get(currentPathToFile);

        Files.deleteIfExists(currentPath);

        Path parentDirectory = currentPath.getParent();

        if (!Files.exists(parentDirectory)) {
            Files.createDirectories(parentDirectory);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = parentDirectory.resolve(newFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save image file: " + newFileName, e);
        }
    }

    public static void deleteFile(
            String filename
    ) throws IOException {
        Path path = Paths.get(filename);
        Files.deleteIfExists(path);

        if (Files.list(path.getParent()).findAny().isEmpty()) {
            Files.delete(path.getParent());
        }
    }

}
