package at.technikum.documentmanager.service;

import at.technikum.documentmanager.dto.TagRequest;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.entity.Tag;
import at.technikum.documentmanager.repository.DocumentRepository;
import at.technikum.documentmanager.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    TagRepository tagRepository;

    @Mock
    DocumentRepository documentRepository;

    @InjectMocks
    TagService tagService;

    @Test
    void createsNewTag() {
        when(tagRepository.findByNameIgnoreCase("Urgent")).thenReturn(Optional.empty());
        when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var tag = tagService.create(new TagRequest("Urgent", "#ff0000"));
        assertNotNull(tag.getId());
        assertEquals("Urgent", tag.getName());
    }

    @Test
    void addsTagToDocument() {
        UUID docId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Document doc = Document.builder()
                .id(docId)
                .originalFilename("file.pdf")
                .contentType("application/pdf")
                .size(1L)
                .storageFilename("a")
                .build();

        Tag tag = Tag.builder().id(tagId).name("Finance").build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var updated = tagService.addTagToDocument(docId, tagId);
        assertTrue(updated.getTags().contains(tag));
    }
}
