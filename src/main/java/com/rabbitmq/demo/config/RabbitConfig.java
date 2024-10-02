package com.rabbitmq.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableRabbit
@Slf4j
public class RabbitConfig {
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(5);    // 设置预取的消息数量为 1，这意味着消费者在处理并确认一条消息之前，不会接收到新的消息。
        factory.setConcurrentConsumers(2);
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // Set up confirm callback
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Message successfully delivered with correlationData: " + correlationData);
            } else {
                log.error("Failed to deliver message with correlationData: " + correlationData + ". Cause: " + cause);
            }
        });

        // Set up return callback
        // mandatory: true: 无法找到符合路由条件的队列，RabbitMQ 会将消息返回给生产者，并触发 ReturnCallback; false: 丢弃
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("Message returned: " + returned.getMessage());
            log.warn("Reply code: " + returned.getReplyCode());
            log.warn("Reply text: " + returned.getReplyText());
            log.warn("Exchange: " + returned.getExchange());
            log.warn("Routing key: " + returned.getRoutingKey());
        });

        return rabbitTemplate;
    }
}
