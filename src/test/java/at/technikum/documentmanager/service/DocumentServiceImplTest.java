package at.technikum.documentmanager.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {
    @Mock
    DocumentRepository repo;
    @InjectMocks
    DocumentServiceImpl service;

    @Test
    void createsDocument() {
        when(repo.save(any())).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        var req = new CreateDocumentRequest("file.pdf", "application/pdf", 123);
        var created = service.create(req);

        System.out.println("Created document ID: " + created.getId());
        System.out.println("Created document size: " + created.getSize());
        System.out.println("Created document uploaded at: " + created.getUploadedAt());
        System.out.println("Created document original filename: " + created.getOriginalFilename());
        assertNotNull(created.getId());
        assertEquals(123, created.getSize());
    }

    @Test
    void getDocument() {
        var id = UUID.randomUUID();
        var d = new Document();
        d.setId(id);
        d.setOriginalFilename("a.pdf");
        d.setContentType("application/pdf");
        d.setSize(123);

        when(repo.findById(id)).thenReturn(java.util.Optional.of(d));

        var found = service.get(id);
        System.out.println("Found document original filename: " + found.getOriginalFilename());
        assertEquals("a.pdf", found.getOriginalFilename());
    }

    @Test
    void getDocumentList() {
        var d1 = new Document();
        d1.setId(UUID.randomUUID());
        d1.setOriginalFilename("a.pdf");
        d1.setContentType("application/pdf");
        d1.setSize(123);

        var d2 = new Document();
        d2.setId(UUID.randomUUID());
        d2.setOriginalFilename("b.pdf");
        d2.setContentType("application/pdf");
        d2.setSize(456);

        when(repo.findAll()).thenReturn(List.of(d1, d2));

        var list = service.list();
        System.out.println("Document list size: " + list.size());
        assertEquals(2, list.size());
    }

    @Test
    void getThrowsWhenNotFound() {
        var id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(java.util.Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(
                java.util.NoSuchElementException.class,
                () -> service.get(id)
        );
    }
}
