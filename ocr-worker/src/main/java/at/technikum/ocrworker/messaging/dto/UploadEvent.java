package at.technikum.ocrworker.messaging.dto;

import java.time.Instant;

public record UploadEvent(
    String documentId,
    String filename,
    String contentType,
    long size,
    Instant uploadedAt,
    String uploadedBy
) {}
