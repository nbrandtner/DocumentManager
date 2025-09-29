package at.technikum.documentmanager.controller;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.dto.DocumentResponse;
import at.technikum.documentmanager.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest r) {
        var d = service.create(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.of(d));
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable UUID id) {
        return DocumentResponse.of(service.get(id));
    }

    @GetMapping
    public List<DocumentResponse> list() {
        return service.list()
                .stream()
                .map(DocumentResponse::of)
                .collect(Collectors.toList());
    }
}