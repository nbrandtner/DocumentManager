package at.technikum.documentmanager.service;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DocumentService {
    private final DocumentRepository repo;
    public DocumentService(DocumentRepository repo){ this.repo = repo; }

    public Document create(CreateDocumentRequest req) {
        var d = new Document();
        d.setOriginalFilename(req.originalFilename());
        d.setContentType(req.contentType());
        d.setSize(req.size());
        return repo.save(d);
    }

    public Document get(UUID id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    public List<Document> list(){ return repo.findAll(); }
}
