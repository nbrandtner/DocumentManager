package at.technikum.documentmanager.service;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;

    @Override
    public Document create(CreateDocumentRequest req) {
        var d = Document.builder()
                .originalFilename(req.originalFilename())
                .contentType(req.contentType())
                .size(req.size())
                .build();

        return repo.save(d);
    }

    @Override
    public Document get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    @Override
    public List<Document> list() {
        return repo.findAll();
    }
}