package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.messaging.dto.UploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class UploadListener {

    private static final Logger log = LoggerFactory.getLogger(UploadListener.class);

    @Value("${app.mq.queue}")
    private String queue;

    @Bean(name = "queueName")
    public String queueName(@Value("${app.mq.queue}") String q) {
        return q;
    }

    // comment:consume events from the queue
    @RabbitListener(queues = "#{@queueName}")
    public void handle(UploadEvent payload) {
        log.info("ocr-worker received docId={} filename={} size={}",
                payload.getDocumentId(), payload.getFilename(), payload.getSize());
    }
}
