package com.rabbitmq.demo.controller;

import com.rabbitmq.demo.service.MessageConsumer;
import com.rabbitmq.demo.service.MessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/rabbitmq")
public class MessageController {
    private final MessageProducer messageProducer;
    private final MessageConsumer messageConsumer;
    private final AmqpAdmin amqpAdmin;
    private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    private static final List<String> CONSUMER_IDS = Arrays.asList("consumer1", "consumer2");


    @PostMapping("/sendMessages")
    public String sendMessages(@RequestParam int numMessages) {
        messageProducer.sendMessages(numMessages);
        return numMessages + " messages sent.";
    }

    @PostMapping("/switchConsumer")
    public String switchConsumer(@RequestParam String consumerId, @RequestParam boolean switchState) {
        try {
            // Determine which consumers to target
            List<String> targetConsumers;
            if ("all".equalsIgnoreCase(consumerId)) {
                targetConsumers = CONSUMER_IDS;
            } else if (CONSUMER_IDS.contains(consumerId.toLowerCase())) {
                targetConsumers = Collections.singletonList(consumerId.toLowerCase());
            } else {
                return "Invalid consumerNo. Please use a valid consumer ID or 'all'.";
            }

            // Start or stop the specified consumers
            for (String item : targetConsumers) {
                if (switchState) {
                    rabbitListenerEndpointRegistry.getListenerContainer(item).start();
                    log.info("{} started.", item);
                } else {
                    rabbitListenerEndpointRegistry.getListenerContainer(item).stop();
                    log.info("{} stopped.", item);
                }
            }

            return String.format("Consumer(s) %s %s.", consumerId, switchState ? "enabled" : "disabled");
        } catch (Exception e) {
            log.error("Error switching consumer: ", e);
            return "An error occurred while switching the consumer(s).";
        }
    }

    @GetMapping("/info")
    public String getRabbitMQInfo() {
        // Replace with your queue name
        String queueName = "demoQueue";

        // Get information about the specified queue
        QueueInformation queueInfo = amqpAdmin.getQueueInfo(queueName);

        if (queueInfo != null) {
            int consumerCount = queueInfo.getConsumerCount();
            int messageCount = queueInfo.getMessageCount();
            boolean consumer1Running = rabbitListenerEndpointRegistry.getListenerContainer("consumer1").isRunning();
            boolean consumer2Running = rabbitListenerEndpointRegistry.getListenerContainer("consumer2").isRunning();

            log.info("Queue Name: {}", queueName);
            log.info("Number of Consumers: {}", consumerCount);
            log.info("Consumer 1 is {}", consumer1Running ? "running" : "stopped");
            log.info("Consumer 2 is {}", consumer2Running ? "running" : "stopped");
            log.info("Number of Messages: {}", messageCount);

            return "Consumer and queue information logged successfully.";
        } else {
            log.warn("Queue '{}' does not exist or cannot be accessed.", queueName);
            return "Queue information could not be retrieved.";
        }
    }
}
