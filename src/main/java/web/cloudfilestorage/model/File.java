package web.cloudfilestorage.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class File implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter(AccessLevel.NONE)
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "description")
    private String description;

    @Column(name = "file")
    private String file;

    @Column(name = "share_link", unique = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String shareLink;

    public File(
            String file,
            String description,
            User owner
    ) {
        this.file = file;
        this.description = description;
        this.owner = owner;
        this.uploadDate = LocalDateTime.now();
        this.shareLink = null;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner", nullable = false)
    @JsonBackReference
    private User owner;

    public File() {
    }


}
