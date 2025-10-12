package at.technikum.documentmanager.controller;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.dto.DocumentResponse;
import at.technikum.documentmanager.messaging.UploadEventPublisher;
import at.technikum.documentmanager.messaging.dto.UploadEvent;
import at.technikum.documentmanager.service.DocumentService;
import at.technikum.documentmanager.service.DocumentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService service;
    private final UploadEventPublisher publisher;

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest r,
                                                   Principal principal) {
        var d = service.create(r);

        var evt = new UploadEvent(
            d.getId().toString(),
            "document-" + d.getId(),
            null,
            0L,
            Instant.now(),
            principal != null ? principal.getName() : "anonymous"
        );

        publisher.publish(evt);

        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.of(d));
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable UUID id) {
        return DocumentResponse.of(service.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable UUID id, @Valid @RequestBody CreateDocumentRequest r) {
        return DocumentResponse.of(service.update(id, r));
    }

    @GetMapping
    public List<DocumentResponse> list() {
        return service.list()
                .stream()
                .map(DocumentResponse::of)
                .collect(Collectors.toList());
    }
}
