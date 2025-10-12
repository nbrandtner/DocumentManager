package at.technikum.documentmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
public class MessagingConfig {

    @Value("${app.mq.exchange}") private String exchangeName;
    @Value("${app.mq.routing}") private String routingKey;
    @Value("${app.mq.queue}") private String queueName;

    @Value("${app.mq.dlx:docs.dlx}") private String dlxName;
    @Value("${app.mq.dlq:docs.uploaded.dlq}") private String dlqName;

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxName);
        args.put("x-dead-letter-routing-key", routingKey + ".dlq");
        return new Queue(queueName, true, false, false, args);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(routingKey);
    }

    @Bean
    public TopicExchange dlx() {
        return new TopicExchange(dlxName, true, false);
    }

    @Bean
    public Queue dlq() {
        return new Queue(dlqName, true);
    }

    @Bean
    public Binding dlBinding() {
        return BindingBuilder.bind(dlq()).to(dlx()).with(routingKey + ".dlq");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter mc) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(mc);
        return tpl;
    }
}
