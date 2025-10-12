package at.technikum.ocrworker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  @Value("${app.mq.exchange:docs.exchange}")
  private String exchange;

  @Value("${app.mq.queue:docs.uploaded.q}")
  private String queue;

  @Value("${app.mq.routing:document.uploaded}")
  private String routingKey;

  @Bean
  public TopicExchange docsExchange() {
    return new TopicExchange(exchange, true, false);
  }

  @Bean
  public Queue docsQueue() {
    return QueueBuilder.durable(queue).build();
  }

  @Bean
  public Binding docsBinding(Queue docsQueue, TopicExchange docsExchange) {
    return BindingBuilder.bind(docsQueue).to(docsExchange).with(routingKey);
  }

  @Bean
  public MessageConverter jackson2MessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      MessageConverter messageConverter) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    return factory;
  }
}
