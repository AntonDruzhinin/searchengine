package searchengine.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "site")
public class SiteModel {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')",
            nullable = false)
    private Status status;

    @Column(name = "status_time",
            nullable = false,
            columnDefinition = "DATETIME")
    private LocalDateTime statusTime;
    @Column(name = "last_error",
            columnDefinition = "TEXT")
    private String lastError;
    @Column(columnDefinition = "VARCHAR(255)")
    private String url;
    @Column(columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany
    @JoinColumn(name = "site_id")
    private List<Page> pages;

}
