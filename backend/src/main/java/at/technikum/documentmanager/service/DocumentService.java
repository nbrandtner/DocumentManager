package at.technikum.documentmanager.service;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.entity.Document;

import java.util.*;

public interface DocumentService {
    Document create(CreateDocumentRequest req);
    Document get(UUID id);
    List<Document> list();
    void delete(UUID id);
    Document update(UUID id, CreateDocumentRequest req);
}
