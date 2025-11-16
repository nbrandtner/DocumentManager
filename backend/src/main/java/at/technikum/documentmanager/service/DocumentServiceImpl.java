package at.technikum.documentmanager.service;

import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.repository.DocumentRepository;
import at.technikum.documentmanager.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;
    private final StorageService storageService;

    @Override
    public Document get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    @Override
    public List<Document> list() {
        return repo.findAll();
    }

    @Override
    public void delete(UUID id) throws IOException {
        var doc = get(id);
        try {
            storageService.delete(doc.getStorageFilename());
        } catch (Exception e) {
            throw new IOException("Failed to delete object from storage: " + e.getMessage(), e);
        }
        repo.deleteById(id);
    }

    @Override
    public Document updateMetadata(UUID id, String newName, String newType) {
        var existing = get(id);
        existing.setOriginalFilename(newName);
        existing.setContentType(newType);
        return repo.save(existing);
    }

    @Override
    public Document replaceFile(UUID id, MultipartFile file) throws IOException {
        var existing = get(id);

        try {
            storageService.delete(existing.getStorageFilename());
        } catch (Exception e) {
            throw new IOException("Failed to delete old object from storage: " + e.getMessage(), e);
        }

        String cleanedName = sanitizeFilename(file.getOriginalFilename());
        String objectName = id + "-" + cleanedName;

        try {
            storageService.store(file.getInputStream(), file.getSize(), file.getContentType(), objectName);
        } catch (Exception e) {
            throw new IOException("Failed to upload new file to storage: " + e.getMessage(), e);
        }

        existing.setOriginalFilename(file.getOriginalFilename());
        existing.setContentType(file.getContentType());
        existing.setSize(file.getSize());
        existing.setStorageFilename(objectName);

        return repo.save(existing);
    }


    @Override
    public Document saveFile(MultipartFile file) throws IOException {
        UUID id = UUID.randomUUID(); // single UUID for both DB and storage
        String cleanedName = sanitizeFilename(file.getOriginalFilename());
        String objectName = id + "-" + cleanedName;

        try {
            storageService.store(file.getInputStream(), file.getSize(), file.getContentType(), objectName);
        } catch (Exception e) {
            throw new IOException("Failed to upload file to storage: " + e.getMessage(), e);
        }

        Document doc = Document.builder()
                .id(id)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .storageFilename(objectName)
                .uploadedAt(Instant.now())
                .build();

        return repo.save(doc);
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unnamed";
        }

        // Separate base name and extension
        int dotIndex = originalFilename.lastIndexOf('.');
        String namePart = (dotIndex > 0) ? originalFilename.substring(0, dotIndex) : originalFilename;
        String extPart = (dotIndex > 0) ? originalFilename.substring(dotIndex) : "";

        // Remove all unsafe characters and replace spaces with underscores
        namePart = namePart.replaceAll("[^a-zA-Z0-9-_]", "_");

        return namePart + extPart;
    }

}
