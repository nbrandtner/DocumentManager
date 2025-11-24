package at.technikum.documentmanager.dto;

import at.technikum.documentmanager.entity.Tag;
import java.util.UUID;

public record TagResponse(
        UUID id,
        String name,
        String color
) {
    public static TagResponse of(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColor());
    }
}
