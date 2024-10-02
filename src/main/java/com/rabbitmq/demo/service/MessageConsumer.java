package com.rabbitmq.demo.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.demo.config.RabbitBindingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {

    @RabbitListener(queues = RabbitBindingConfig.QUEUE_A_NAME, id = "consumer1", ackMode = "MANUAL")
    public void receiveMessageA(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("Consumer A received: " + message);
            // 模拟消息处理
            Thread.sleep(1000);
            throw new RuntimeException("Consumer A throw");
            // log.info("Consumer A done: " + message);
            // channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 如果处理失败，拒绝消息并重新入队
            log.info("Consumer A error: " + message);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(queues = RabbitBindingConfig.QUEUE_B_NAME, id = "consumer2")
    public void receiveMessageB(String message) {
        log.info("Consumer B received: " + message);
        log.info("Consumer B done: " + message);
    }
}