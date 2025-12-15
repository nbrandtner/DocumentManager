package at.technikum.documentmanager.integrationtest;
import at.technikum.documentmanager.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class DocumentUploadTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    DocumentRepository documentRepository;

    @MockitoSpyBean RabbitTemplate rabbitTemplate;

    @Value("${APP_MQ_EXCHANGE:docs.exchange}")
    String exchange;

    @Value("${APP_MQ_ROUTING:document.uploaded}")
    String routingKey;

    @Test
    void upload_shouldReturn201_storeInDb_andPublishMessage() throws Exception {
        var file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4\n% test\n".getBytes()
        );

        var result = mockMvc.perform(multipart("/api/documents/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.originalFilename").value("test.pdf"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String idStr = body.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        UUID id = UUID.fromString(idStr);

        assertThat(documentRepository.findById(id)).isPresent();

        verify(rabbitTemplate, timeout(2000).atLeastOnce())
                .convertAndSend(eq(exchange), eq(routingKey), any(Object.class));
    }
}
