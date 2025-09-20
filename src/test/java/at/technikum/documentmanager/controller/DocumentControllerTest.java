package at.technikum.documentmanager.controller;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import at.technikum.documentmanager.dto.CreateDocumentRequest;
import at.technikum.documentmanager.entity.Document;
import at.technikum.documentmanager.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    DocumentService service;

    @Test
    void createReturns201() throws Exception {
        var d = new Document();
        d.setId(UUID.randomUUID());
        d.setOriginalFilename("a.pdf");
        d.setContentType("application/pdf");
        d.setSize(123);

        when(service.create(any(CreateDocumentRequest.class))).thenReturn(d);

        mvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalFilename\":\"a.pdf\",\"contentType\":\"application/pdf\",\"size\":123}"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getByIdReturnsDocument() throws Exception {
        var id = UUID.randomUUID();
        var d = new Document();
        d.setId(id);
        d.setOriginalFilename("hello.pdf");
        d.setContentType("application/pdf");
        d.setSize(321);

        when(service.get(id)).thenReturn(d);

        mvc.perform(get("/api/documents/" + id))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.originalFilename").value("hello.pdf"))
                .andExpect(jsonPath("$.size").value(321));
    }

    @Test
    void listReturnsAllDocuments() throws Exception {
        var d1 = new Document();
        d1.setId(UUID.randomUUID());
        d1.setOriginalFilename("doc1.pdf");
        d1.setContentType("application/pdf");
        d1.setSize(111);

        var d2 = new Document();
        d2.setId(UUID.randomUUID());
        d2.setOriginalFilename("doc2.pdf");
        d2.setContentType("application/pdf");
        d2.setSize(222);

        when(service.list()).thenReturn(List.of(d1, d2));

        mvc.perform(get("/api/documents"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].originalFilename").value("doc1.pdf"))
                .andExpect(jsonPath("$[1].originalFilename").value("doc2.pdf"))
                .andExpect(jsonPath("$[0].size").value(111))
                .andExpect(jsonPath("$[1].size").value(222))
                .andExpect(jsonPath("$[0].id").value(d1.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(d2.getId().toString()));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new NoSuchElementException("Document not found"));

        mvc.perform(get("/api/documents/" + id))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Document not found"));
    }

    @Test
    void createReturns400WhenOriginalFilenameBlank() throws Exception {
        // Original filename is blank -> @NotBlank should trigger MethodArgumentNotValidException
        mvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          {"originalFilename":"","contentType":"application/pdf","size":123}
                          """))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                // Default message for @NotBlank is usually "must not be blank"
                .andExpect(jsonPath("$.error").value("must not be blank"));
    }

    @Test
    void createReturns400WhenSizeNegative() throws Exception {
        // Size is negative -> @Positive should trigger MethodArgumentNotValidException
        mvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          {"originalFilename":"x.pdf","contentType":"application/pdf","size":-1}
                          """))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                // Default message for @Positive is usually "must be greater than 0"
                .andExpect(jsonPath("$.error").value("must be greater than 0"));
    }
}
