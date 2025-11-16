package at.technikum.documentmanager.service;

import at.technikum.documentmanager.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

public interface DocumentService {
    Document get(UUID id);
    List<Document> list();
    void delete(UUID id) throws IOException;
    Document updateMetadata(UUID id, String newName, String newType);
    Document replaceFile(UUID id, MultipartFile file) throws IOException;
    Document saveFile(MultipartFile file) throws IOException;
    void saveSummary(UUID docId, String summary);
}
