package at.technikum.documentmanager.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import at.technikum.documentmanager.service.DocumentService;
import org.springframework.amqp.core.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class SummaryListener {

    private final ObjectMapper objectMapper;
    private final DocumentService documentService;

    @RabbitListener(queues = "summary-results")
    public void receive(Message message) {
        try {
            String json = new String(message.getBody());
            SummaryMessage msg = objectMapper.readValue(json, SummaryMessage.class);

            log.info("[SummaryListener] Received summary for {}", msg.getDocumentId());
            documentService.saveSummary(msg.getDocumentId(), msg.getSummary());
            log.info("[SummaryListener] Summary saved.");

        } catch (Exception e) {
            log.error("[SummaryListener] Failed to process summary message: {}", new String(message.getBody()), e);
        }
    }
}