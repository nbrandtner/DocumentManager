package at.technikum.documentmanager.service;

import at.technikum.documentmanager.dto.TagRequest;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.entity.Tag;
import at.technikum.documentmanager.repository.DocumentRepository;
import at.technikum.documentmanager.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final DocumentRepository documentRepository;

    public Tag create(TagRequest request) {
        tagRepository.findByNameIgnoreCase(request.name())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tag already exists: " + existing.getName());
                });

        Tag tag = Tag.builder()
                .id(UUID.randomUUID())
                .name(request.name().trim())
                .color(request.color())
                .build();
        return tagRepository.save(tag);
    }

    public List<Tag> listAll() {
        return tagRepository.findAll();
    }

    @Transactional
    public Document addTagToDocument(UUID documentId, UUID tagId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchElementException("Tag not found"));

        document.getTags().add(tag);
        return documentRepository.save(document);
    }

    @Transactional
    public Document removeTagFromDocument(UUID documentId, UUID tagId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchElementException("Tag not found"));

        document.getTags().remove(tag);
        return documentRepository.save(document);
    }

    public List<Tag> tagsForDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
        return document.getTags().stream().toList();
    }
}
