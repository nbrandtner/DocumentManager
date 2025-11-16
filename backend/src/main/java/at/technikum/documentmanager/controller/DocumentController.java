package at.technikum.documentmanager.controller;

import at.technikum.documentmanager.dto.DocumentResponse;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.messaging.UploadEventPublisher;
import at.technikum.documentmanager.messaging.dto.UploadEvent;
import at.technikum.documentmanager.repository.DocumentRepository;
import at.technikum.documentmanager.service.DocumentService;
import at.technikum.documentmanager.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;


import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService service;
    private final UploadEventPublisher publisher;
    private final StorageService storageService;
    private final DocumentRepository documentRepository;

    @PostMapping("/upload")
    public ResponseEntity<Document> upload(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        // Save file to MinIO and persist metadata
        Document doc = service.saveFile(file);

        // Publish upload event (RabbitMQ or similar)
        publisher.publish(new UploadEvent(
                doc.getId().toString(),
                doc.getOriginalFilename(),
                doc.getContentType(),
                file.getSize(),
                Instant.now(),
                principal != null ? principal.getName() : "unknown"
        ));

        log.info("Uploaded document '{}' ({} bytes) and published event", doc.getOriginalFilename(), file.getSize());
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @GetMapping("/{id}")
    public Document get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        try {
            // Retrieve metadata via service
            Document doc = service.get(id);
            if (doc == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            // Load the file from MinIO
            Optional<InputStream> in = storageService.load(doc.getStorageFilename());
            if (in.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            // Wrap stream into Spring resource
            InputStreamResource resource = new InputStreamResource(in.get());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Download failed for document {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<String> getSummary(@PathVariable UUID id) {
        return documentRepository.findById(id)
                .map(doc -> ResponseEntity.ok(doc.getSummary()))
                .orElse(ResponseEntity.notFound().build());
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

    @GetMapping
    public List<DocumentResponse> list() {
        return service.list()
                .stream()
                .map(DocumentResponse::of)
                .collect(Collectors.toList());
    }
}
