package at.technikum.ocrworker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class RabbitConfig {

  @Value("${app.mq.exchange:docs.exchange}") private String exchange;
  @Value("${app.mq.queue:docs.uploaded.q}") private String queue;
  @Value("${app.mq.routing:document.uploaded}") private String routing;
  @Value("${app.mq.dlx:docs.dlx}") private String dlx;
  @Value("${app.mq.dlq:docs.uploaded.dlq}") private String dlq;

  @Bean
  public TopicExchange docsExchange() {
    return new TopicExchange(exchange, true, false);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(dlx, true, false);
  }

  @Bean
  public Queue docsQueue() {
    return QueueBuilder.durable(queue)
            .withArgument("x-dead-letter-exchange", dlx)
            .withArgument("x-dead-letter-routing-key", routing + ".dlq")
            .build();
  }

  @Bean
  public Queue dlqQueue() {
    return new Queue(dlq, true);
  }

  @Bean
  public Binding binding() {
    return BindingBuilder.bind(docsQueue()).to(docsExchange()).with(routing);
  }

  @Bean
  public Binding dlBinding() {
    return BindingBuilder.bind(dlqQueue()).to(deadLetterExchange()).with(routing + ".dlq");
  }

  @Bean
  public MessageConverter jackson2MessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}

