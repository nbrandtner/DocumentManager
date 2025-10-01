package at.technikum.documentmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateDocumentRequest(
        @NotBlank String originalFilename,
        @NotBlank String contentType,
        @Positive long size
) {}