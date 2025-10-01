package at.technikum.documentmanager.dto;

import at.technikum.documentmanager.entity.Document;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id, String originalFilename, String contentType, long size, Instant uploadedAt
){
    public static DocumentResponse of(Document d){
        return new DocumentResponse(d.getId(), d.getOriginalFilename(), d.getContentType(), d.getSize(), d.getUploadedAt());
    }
}