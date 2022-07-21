package web.cloudfilestorage.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Data
@Builder
@AllArgsConstructor
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

    public File(
            String file,
            String description,
            User owner
    ) {
        this.file = file;
        this.description = description;
        this.owner = owner;
        this.uploadDate = LocalDateTime.now();
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner", nullable = false)
    @JsonBackReference
    private User owner;

    public File() {
    }


}
