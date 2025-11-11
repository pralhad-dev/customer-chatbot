package com.chatbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component("kafkaErrorHandler")
public class KafkaErrorHandler implements KafkaListenerErrorHandler {

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.error("Kafka Listener Error - Topic: {}, Payload: {}, Error: {}",
                message.getHeaders().get("kafka_receivedTopic"),
                message.getPayload(),
                exception.getMessage());

        return null;
    }
}