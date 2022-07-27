package web.cloudfilestorage.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import web.cloudfilestorage.dto.file.FileData;
import web.cloudfilestorage.model.File;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.repository.FileRepository;
import web.cloudfilestorage.repository.UserRepository;
import web.cloudfilestorage.utils.FileUtil;

import javax.persistence.EntityNotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Value("${filesRoot}")
    @Setter
    @Getter
    private String filesRoot;

    @Autowired
    public FileService(
            UserRepository userRepository,
            FileRepository fileRepository
    ) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public List<File> findAllFiles() {
        return fileRepository.findAll();
    }

    public List<File> allOwnerFiles(String username) {
        return fileRepository.findAllByOwner_Username(username);
    }

    public File findById(long id) throws EntityNotFoundException {
        Optional<File> file = fileRepository.findFileById(id);
        if (file.isEmpty()) {
            throw new EntityNotFoundException(
                    "File with id " + id + " is not present in database!"
            );
        }
        return file.get();
    }

    public File findByLink(String link) throws EntityNotFoundException {
        Optional<File> file = fileRepository.findFileByShareLink(link);
        if (file.isEmpty()) {
            throw new EntityNotFoundException(
                    "Link " + link + " is not valid!"
            );
        }
        return file.get();
    }

    public String generateLink(long id) throws EntityNotFoundException {
        File file = findById(id);
        String link = UUID.randomUUID().toString();
        file.setShareLink(link);
        fileRepository.save(file);
        return link;
    }

    public Resource download(long id)
            throws EntityNotFoundException, FileNotFoundException {
        File file = findById(id);
        return getResource(file);
    }

    public Resource download(String link)
            throws EntityNotFoundException, FileNotFoundException {
        File file = findByLink(link);
        return getResource(file);
    }

    private Resource getResource(File file) throws FileNotFoundException {
        try {
            Path filePath = Path.of(file.getFile());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException(
                        "Could not read file: " + file.getFile());
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not read file: " + file.getFile());
        }
    }

    public File create(
            String username,
            MultipartFile multipartFile,
            FileData fileData
    ) throws IOException, EntityNotFoundException {
        Optional<User> ownerData = userRepository.findUserByUsername(username);
        if (ownerData.isEmpty()) {
            throw new EntityNotFoundException(
                    "User " + username + " is not present in database!"
            );
        }
        User owner = ownerData.get();

        String fileName = null;
        if (multipartFile != null) {
            fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            String uploadDir = getFilesRoot() + owner.getId() + "/";
            FileUtil.saveFile(uploadDir, fileName, multipartFile);
        }

        File file = new File(
                getFilesRoot() + owner.getId() + "/" + fileName,
                fileData.getDescription(),
                owner
        );
        return fileRepository.save(file);
    }

    public File update(
            File file,
            MultipartFile multipartFile,
            FileData fileData
    ) throws IOException, EntityNotFoundException {

        file.setDescription(fileData.getDescription());

        if (multipartFile != null) {
            String newFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            FileUtil.updateFile(file.getFile(), newFileName, multipartFile);
            file.setFile(getFilesRoot() + file.getOwner().getId() + "/" + newFileName);
            return fileRepository.save(file);
        }
        return fileRepository.save(file);
    }

    public void delete(
            File file
    ) throws IOException, EntityNotFoundException {
        FileUtil.deleteFile(file.getFile());
        fileRepository.deleteById(file.getId());
    }

}
