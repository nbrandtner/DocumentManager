package at.technikum.documentmanager.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class IndexedDocument {
    private String documentId;
    private String filename;
    private String contentType;
    private Instant uploadedAt;
    private Long size;
    private String text;
    private String summary;
}
