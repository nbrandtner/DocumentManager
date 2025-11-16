package at.technikum.documentmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableRabbit
public class MessagingConfig {

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
    @Bean
    public ApplicationRunner rabbitAdminInitializer(AmqpAdmin amqpAdmin) {
        return args -> {
            log.info("Initializing RabbitMQ exchanges and queues...");
            amqpAdmin.initialize();
        };
    }

    @Value("${app.mq.exchange:docs.exchange}")
    private String exchangeName;

    @Value("${app.mq.queue:docs.uploaded.q}")
    private String queueName;

    @Value("${app.mq.routing:document.uploaded}")
    private String routingKey;

    @Value("${app.mq.dlx:docs.dlx}")
    private String dlxName;

    @Value("${app.mq.dlq:docs.uploaded.dlq}")
    private String dlqName;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(dlxName, true, false);
    }

    @Bean
    public Queue uploadedQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", routingKey + ".dlq")
                .build();
    }

    @Bean
    public Queue genaiTasksQueue() {
        return QueueBuilder.durable("genai-tasks").build();
    }

    @Bean
    public Queue summaryResultsQueue() {
        return QueueBuilder.durable("summary-results").build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(dlqName, true);
    }

    @Bean
    public Binding uploadBinding() {
        return BindingBuilder.bind(uploadedQueue()).to(exchange()).with(routingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(routingKey + ".dlq");
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter mc) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(mc);
        return tpl;
    }
}
