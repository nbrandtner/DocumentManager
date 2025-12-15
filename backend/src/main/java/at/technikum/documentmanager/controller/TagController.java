package at.technikum.documentmanager.controller;

import at.technikum.documentmanager.dto.TagRequest;
import at.technikum.documentmanager.entity.Tag;
import at.technikum.documentmanager.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tag create(@Valid @RequestBody TagRequest request) {
        return tagService.create(request);
    }

    @GetMapping
    public List<Tag> list() {
        return tagService.listAll();
    }
}
