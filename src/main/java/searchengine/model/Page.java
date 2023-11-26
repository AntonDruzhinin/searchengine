package searchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Table( name = "page", indexes = @Index(name = "path_index", columnList = "path", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class Page {
//    public Page(SiteModel siteModel) {
//        this.siteModel = siteModel;
//    }

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "site_id",
//            nullable = false)
//    private SiteModel siteModel;

    @Column(columnDefinition = "VARCHAR(250)",
            nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(name = "content",
            columnDefinition = "MEDIUMTEXT",
            nullable = false)
    private String content;


}
