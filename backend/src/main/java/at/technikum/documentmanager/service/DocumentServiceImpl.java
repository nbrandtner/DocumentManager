package at.technikum.documentmanager.service;

import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;

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
        Path path = Paths.get("/app/uploads").resolve(doc.getStorageFilename());
        Files.deleteIfExists(path);
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

        // delete old file
        Path oldPath = Paths.get("/app/uploads").resolve(existing.getStorageFilename());
        Files.deleteIfExists(oldPath);

        // save new file
        String storageName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path uploadDir = Paths.get("/app/uploads");
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(storageName), StandardCopyOption.REPLACE_EXISTING);

        // update metadata
        existing.setOriginalFilename(file.getOriginalFilename());
        existing.setContentType(file.getContentType());
        existing.setSize(file.getSize());
        existing.setStorageFilename(storageName);

        return repo.save(existing);
    }

    @Override
    public Document saveFile(MultipartFile file) throws IOException {
        Path uploadDir = Paths.get("/app/uploads");
        Files.createDirectories(uploadDir);

        String storageName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(storageName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Document doc = Document.builder()
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .storageFilename(storageName)
                .uploadedAt(Instant.now())
                .build();

        return repo.save(doc);
    }
}
