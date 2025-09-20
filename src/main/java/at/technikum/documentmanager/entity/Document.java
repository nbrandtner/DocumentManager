package at.technikum.documentmanager.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();

    // getters & setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String f) { this.originalFilename = f; }
    public String getContentType() { return contentType; }
    public void setContentType(String c) { this.contentType = c; }
    public long getSize() { return size; }
    public void setSize(long s) { this.size = s; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant t) { this.uploadedAt = t; }
}
