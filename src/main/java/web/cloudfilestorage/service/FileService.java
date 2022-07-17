package web.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileService{

    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Value("${filesRoot}")
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

    public File fileById (long id) throws EntityNotFoundException {
        Optional<File> file =  fileRepository.findFileById(id);
        if (file.isEmpty()) {
            throw new EntityNotFoundException(
                    "File with id " + id + " is not present in database!"
            );
        }
        return file.get();
    }

    public List<File> allOwnerFiles (String username) {
        return fileRepository.findAllByOwner_Username(username);
    }

    public File create (
            String username,
            MultipartFile multipartFile,
            FileData fileData
    ) throws IOException, EntityNotFoundException {
        Optional<User> ownerData = userRepository.findUserByUsername(username);
        if (ownerData.isEmpty()) {
            throw new EntityNotFoundException(
                    "User " + username + " is not present in the database!"
            );
        }
        User owner = ownerData.get();

        String fileName = null;
        if (multipartFile != null) {
            fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            String uploadDir = filesRoot + owner.getId();
            FileUtil.saveFile(uploadDir, fileName, multipartFile);
        }

        File file = new File(
                fileName,
                fileData.getDescription(),
                owner
        );
        return fileRepository.save(file);
    }

    public File update (
            File file,
            MultipartFile multipartFile,
            FileData fileData
    ) throws IOException, EntityNotFoundException {

        file.setDescription(fileData.getDescription());

        if (multipartFile != null) {
            String newFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            String currentPath = filesRoot + file.getOwner().getId() + "/" + file.getFile();
            FileUtil.updateFile(currentPath, newFileName, multipartFile);
            file.setFile(newFileName);
            return fileRepository.save(file);
        }
        return fileRepository.save(file);
    }

    public void delete (
            File file
    ) throws IOException, EntityNotFoundException {
        FileUtil.deleteFile(
                filesRoot + file.getOwner().getId() + "/" + file.getFile());
        fileRepository.deleteById(file.getId());
    }

}
