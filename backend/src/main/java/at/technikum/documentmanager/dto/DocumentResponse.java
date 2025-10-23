package at.technikum.documentmanager.dto;

import at.technikum.documentmanager.entity.Document;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String originalFilename,
        String contentType,
        long size,
        String downloadUrl,
        Instant uploadedAt
) {
    // Basic mapping (used for list endpoints, no presigned URL)
    public static DocumentResponse of(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSize(),
                null, // downloadUrl not included in list responses
                d.getUploadedAt()
        );
    }

    // Overloaded factory for responses that include a presigned download URL
    public static DocumentResponse of(Document d, String downloadUrl) {
        return new DocumentResponse(
                d.getId(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSize(),
                downloadUrl,
                d.getUploadedAt()
        );
    }
}
