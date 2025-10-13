package at.technikum.documentmanager.messaging;

import at.technikum.documentmanager.messaging.dto.UploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UploadEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UploadEventPublisher.class);

    private final RabbitTemplate rabbit;
    private final String exchange;
    private final String routing;

    public UploadEventPublisher(
            RabbitTemplate rabbit,
            @Value("${app.mq.exchange}") String exchange,
            @Value("${app.mq.routing}") String routing
    ) {
        this.rabbit = rabbit;
        this.exchange = exchange;
        this.routing = routing;
    }

    public void publish(UploadEvent evt) {
        try {
            rabbit.convertAndSend(exchange, routing, evt);
            log.info("upload-event published docId={} filename={}", evt.documentId(), evt.filename());
        } catch (AmqpException ex) {
            log.error("failed to publish upload-event docId={} cause={}", evt.documentId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
