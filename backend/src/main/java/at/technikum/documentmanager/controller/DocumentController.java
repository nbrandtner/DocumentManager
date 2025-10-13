package at.technikum.documentmanager.controller;

import at.technikum.documentmanager.dto.DocumentResponse;
import at.technikum.documentmanager.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import at.technikum.documentmanager.entity.Document;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @PostMapping("/upload")
    public ResponseEntity<Document> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Document doc = service.saveFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @GetMapping
    public List<Document> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Document get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) throws IOException {
        Document doc = service.get(id);
        Path path = Paths.get("/app/uploads").resolve(doc.getStorageFilename());
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}/metadata")
    public Document updateMetadata(@PathVariable UUID id,
                                   @RequestParam String newName,
                                   @RequestParam String newType) {
        return service.updateMetadata(id, newName, newType);
    }

    @PutMapping("/{id}/replace")
    public Document replaceFile(@PathVariable UUID id,
                                @RequestParam("file") MultipartFile file) throws IOException {
        return service.replaceFile(id, file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws IOException {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
