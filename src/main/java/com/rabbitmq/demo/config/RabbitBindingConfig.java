package com.rabbitmq.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitBindingConfig {
    public static final String QUEUE_A_NAME = "queueA";
    public static final String QUEUE_B_NAME = "queueB";

    public static final String ROUTING_KEY_A = "a.a.routingKey";
    public static final String ROUTING_KEY_B = "b.a.routingKey";
    public static final String ROUTING_KEY_C = "a.routingKey.info";
    public static final String ROUTING_KEY_D = "a.routingKey.info.xxx";

    private final RabbitAdmin rabbitAdmin;

    @PostConstruct
    public void clearRabbitMQResources() {
        rabbitAdmin.deleteQueue("queueA");
        rabbitAdmin.deleteQueue("queueB");
        rabbitAdmin.deleteExchange("exchangeA");
        rabbitAdmin.deleteExchange("exchangeB");
        rabbitAdmin.deleteExchange("exchangeC");
        rabbitAdmin.deleteExchange("exchangeD");
    }

    @Bean
    public Queue queueA() {
        // return new Queue(QUEUE_A_NAME, false, false, true);
        // return new AnonymousQueue();
        return new Queue(QUEUE_A_NAME, true);
    }

    @Bean
    public Queue queueB() {
        return new Queue(QUEUE_B_NAME, true);
    }

    /**
     * Fanout: 广播，发送到所有绑定到此exchange的队列
     */
    @Bean
    public FanoutExchange exchangeA() {
        return new FanoutExchange("exchangeA");
    }

    /**
     * Direct: 通过routingKey(exchange)精确匹配bindingKey(queue)，决定消息发送到哪个队列<br>
     * name: exchange名称，指定消息发送到哪个exchange
     */
    @Bean
    public DirectExchange exchangeB() {
        return new DirectExchange("exchangeB");
    }

    /**
     * topic:主题交换器,使用特定路由键发送的消息将被发送到所有使用匹配绑定键绑定的队列<br>
     * *: 匹配一个单词, #: 匹配0个或多个单词
     */
    @Bean
    public TopicExchange exchangeC() {
        return new TopicExchange("exchangeC");
    }

    /**
     * headers: 通过header属性匹配发送到指定队列
     */
    @Bean
    public HeadersExchange exchangeD() {
        return new HeadersExchange("exchangeD");
    }

    @Bean
    public Binding bindingA() {
        return BindingBuilder.bind(queueA()).to(exchangeB()).with(ROUTING_KEY_A);
        // return BindingBuilder.bind(queueA()).to(exchangeA());
    }

    /**
     * 为什么需要routingKey: 可以有多个队列使用不同的 binding key 绑定到同一个exchange。
     * 生产者可以根据 routing key 有针对性地将消息发送到特定的队列。
     */
    @Bean
    public Binding bindingB() {
        return BindingBuilder.bind(queueB()).to(exchangeB()).with(ROUTING_KEY_B);
        // return BindingBuilder.bind(queueB()).to(exchangeB()).with(ROUTING_KEY_A);
        // return BindingBuilder.bind(queueB()).to(exchangeA());
    }

    @Bean
    public Binding bindingC() {
        // return BindingBuilder.bind(queueB()).to(exchangeC()).with(ROUTING_KEY_B);
        return BindingBuilder.bind(queueB()).to(exchangeC()).with("*.routingKey.#");
        // return BindingBuilder.bind(queueB()).to(exchangeA());
    }

    @Bean
    public Binding bindingD() {
        Map<String, Object> headerValues = new HashMap<>();
        headerValues.put("keyA", "valueA");
        headerValues.put("keyB", "valueB");

        return BindingBuilder.bind(queueB()).to(exchangeD())
                .whereAny(headerValues).match();
    }
}
