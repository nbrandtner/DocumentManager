package at.technikum.documentmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 20) String color
) {
}
