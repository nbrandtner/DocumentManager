package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.messaging.dto.UploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UploadListener {

    private static final Logger log = LoggerFactory.getLogger(UploadListener.class);

    @Value("${app.mq.queue:docs.uploaded.q}")
    private String queueName;

    @RabbitListener(queues = "${app.mq.queue:docs.uploaded.q}")
    public void handle(UploadEvent evt) {
        log.info("OCR worker received: docId={} filename={}", evt.documentId(), evt.filename());
        //todo ocr here
    }
}
