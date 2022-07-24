package web.cloudfilestorage.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.cloudfilestorage.dto.file.FileData;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.model.File;
import web.cloudfilestorage.model.User;
import web.cloudfilestorage.service.FileService;
import web.cloudfilestorage.service.UserService;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File", description = "Files' operations")
public class FileController {

    private final UserService userService;
    private final FileService fileService;

    @Autowired
    public FileController(
            UserService userService,
            FileService fileService
    ) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping("")
    @Operation(
            summary = "List files",
            description = "List authenticated user's files"
    )
    public ResponseEntity<List<File>> list(
            Authentication authentication
    ) throws JwtAuthenticationException {

        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }

        return new ResponseEntity<>(
                fileService.allOwnerFiles(authentication.getName()),
                HttpStatus.OK
        );
    }

    @PostMapping("")
    @Operation(
            summary = "Create file",
            description = "Create file owned by an authorized user"
    )
    public ResponseEntity<File> create(
            @RequestParam(value = "file")
            MultipartFile multipartFile,
            FileData fileMetadata,
            Authentication authentication
    )
            throws
            JwtAuthenticationException,
            IOException {

        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }
        return new ResponseEntity<>(
                fileService.create(
                        authentication.getName(),
                        multipartFile,
                        fileMetadata
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update file",
            description = "Update file owned by an authorized user"
    )
    public ResponseEntity<File> update(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided as path variable")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id,
            @RequestParam(value = "file")
            MultipartFile multipartFile,
            FileData fileMetadata,
            Authentication authentication
    )
            throws
            JwtAuthenticationException,
            IOException,
            EntityNotFoundException,
            AccessDeniedException {

        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }
        File file = fileService.fileById(id);
        User owner = file.getOwner();

        if (owner != userService.findByUsername(authentication.getName())) {
            throw new AccessDeniedException(
                    "Permission denied!"
            );
        }

        return new ResponseEntity<>(
                fileService.update(
                        file,
                        multipartFile,
                        fileMetadata
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete file",
            description = "Delete file owned by an authorized user"
    )
    public ResponseEntity<String> delete(
            @PathVariable(value = "id")
            @NotNull(message = "id must be provided as path variable")
            @Min(value = 1, message = "minimal value for id is 1")
            Long id,
            Authentication authentication
    )
            throws
            JwtAuthenticationException,
            EntityNotFoundException,
            AccessDeniedException,
            IOException {
        if (authentication == null) {
            throw new JwtAuthenticationException("Not authenticated!", "Authorization");
        }
        File file = fileService.fileById(id);
        User owner = file.getOwner();

        if (owner != userService.findByUsername(authentication.getName())) {
            throw new AccessDeniedException(
                    "Permission denied!"
            );
        }
        fileService.delete(file);
        return new ResponseEntity<>(
                "File " + file.getFile() + " deleted!",
                HttpStatus.NO_CONTENT
        );
    }

}
