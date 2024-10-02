package com.rabbitmq.demo.service;

import com.rabbitmq.demo.config.RabbitBindingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProducer {
    private final RabbitTemplate rabbitTemplate;
    private final Queue queueA;
    private final Queue queueB;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // 5 线程池

    public static final String CORRELATION_ID_A = "correlationId-A";
    public static final String CORRELATION_ID_B = "correlationId-B";

    public void sendMessages(int numberOfMessages) {
        for (int i = 0; i < numberOfMessages; i++) {
            final int messageNumber = i;
            executorService.submit(() -> {
                String message = "Message " + messageNumber;
                rabbitTemplate.convertAndSend("exchangeA", RabbitBindingConfig.ROUTING_KEY_A, message);
                log.info("Sent: " + message);
            });
        }
    }

    @Scheduled(fixedDelay = 100000, initialDelay = 500)
    public void send() throws ExecutionException, InterruptedException, TimeoutException {
        String message = "Hello World!";

        // 使用默认交换器直接发送到队列，exchangeName=""
        // rabbitTemplate.convertAndSend(queueA.getName(), message + "1");
        // Thread.sleep(1000);
        // rabbitTemplate.convertAndSend(queueA.getName(), message + "2");
        // Thread.sleep(1000);
        // rabbitTemplate.convertAndSend(queueA.getName(), message + "3");
        // Thread.sleep(1000);
        // rabbitTemplate.convertAndSend(queueA.getName(), message + "4");
        // Thread.sleep(1000);

        // fanout模式，发送到所有绑定到此exchange的队列,不考虑routingKey
        // rabbitTemplate.convertAndSend("exchangeA", RabbitBindingConfig.ROUTING_KEY_A, message + "1");
        // rabbitTemplate.convertAndSend("exchangeA", "", message + "2");

        // direct
        // rabbitTemplate.convertAndSend("exchangeB", RabbitBindingConfig.ROUTING_KEY_A, message + "1");
        // rabbitTemplate.convertAndSend("exchangeB", RabbitBindingConfig.ROUTING_KEY_B, message + "2");
        // rabbitTemplate.convertAndSend("exchangeB", RabbitBindingConfig.ROUTING_KEY_C, message + "3");

        // topic
        // rabbitTemplate.convertAndSend("exchangeC", RabbitBindingConfig.ROUTING_KEY_A, message + "1");
        // rabbitTemplate.convertAndSend("exchangeC", RabbitBindingConfig.ROUTING_KEY_B, message + "2");
        // rabbitTemplate.convertAndSend("exchangeC", RabbitBindingConfig.ROUTING_KEY_C, message + "3");
        // rabbitTemplate.convertAndSend("exchangeC", RabbitBindingConfig.ROUTING_KEY_D, message + "4");

        // headers
        // Message message = MessageBuilder.withBody("Hello World! Headers".getBytes())
        //         .setHeader("keyA", "valueA")
        //         .setHeader("keyB", "valueB1")
        //         .build();
        // rabbitTemplate.send("exchangeD", "", message);

        // log.info(" [x] Sent '" + message + "'");

        // publisher confirm
        CorrelationData correlationDataA = new CorrelationData(CORRELATION_ID_A);
        // CorrelationData correlationDataB = new CorrelationData(CORRELATION_ID_B);
        rabbitTemplate.convertAndSend("exchangeB", RabbitBindingConfig.ROUTING_KEY_A, message + "1", correlationDataA);
        // rabbitTemplate.convertAndSend("exchangeB", RabbitBindingConfig.ROUTING_KEY_A, message + "2", correlationDataB);
        log.info(" [x] Sent '" + message + "'");
        CorrelationData.Confirm confirmA = correlationDataA.getFuture().get(10, TimeUnit.SECONDS);
        // CorrelationData.Confirm confirmB = correlationDataB.getFuture().get(10, TimeUnit.SECONDS);
        log.info("Confirm A received for good delivery, ack = " + confirmA.isAck());
        // log.info("Confirm B received for good delivery, ack = " + confirmB.isAck());
    }
}
