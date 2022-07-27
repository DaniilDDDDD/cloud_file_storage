package web.cloudfilestorage.dto.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import web.cloudfilestorage.model.File;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileView {

    private Long id;

    private Long owner;

    private LocalDateTime uploadDate;

    private String description;

    private String file;

    private String shareLink;

    public FileView(File file) {
        this.id = file.getId();
        this.owner = file.getOwner().getId();
        this.uploadDate = file.getUploadDate();
        this.description = file.getDescription();
        this.file = file.getFile();
        this.shareLink = file.getShareLink();
    }
}
