package at.technikum.ocrworker.messaging.dto;

import java.time.Instant;

// comment:minimal dto matching backend event
public class UploadEvent {
    private String documentId;
    private String filename;
    private String contentType;
    private long size;
    private Instant uploadedAt;
    private String uploadedBy;

    public UploadEvent() {}

    public String getDocumentId() { return documentId; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public Instant getUploadedAt() { return uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }

    public void setDocumentId(String v) { this.documentId = v; }
    public void setFilename(String v) { this.filename = v; }
    public void setContentType(String v) { this.contentType = v; }
    public void setSize(long v) { this.size = v; }
    public void setUploadedAt(Instant v) { this.uploadedAt = v; }
    public void setUploadedBy(String v) { this.uploadedBy = v; }
}
