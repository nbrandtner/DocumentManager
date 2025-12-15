package at.technikum.documentmanager.search;

import java.time.Instant;
import java.util.UUID;

public record SearchResult(
        UUID id,
        String filename,
        String contentType,
        Instant uploadedAt,
        String snippet,
        double score
) {
}
