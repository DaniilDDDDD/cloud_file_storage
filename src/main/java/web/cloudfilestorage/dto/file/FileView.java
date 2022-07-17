package web.cloudfilestorage.dto.file;

import lombok.Data;
import web.cloudfilestorage.model.File;

import java.time.LocalDateTime;

@Data
public class FileView {

    private Long id;

    private Long owner;

    private LocalDateTime uploadDate;

    private String description;

    private String file;

    public FileView(File file) {
        this.id = file.getId();
        this.owner = file.getOwner().getId();
        this.uploadDate = file.getUploadDate();
        this.description = file.getDescription();
        this.file = file.getFile();
    }
}
