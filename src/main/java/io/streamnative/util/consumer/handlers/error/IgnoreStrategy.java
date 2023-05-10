package io.streamnative.util.consumer.handlers.error;

import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreStrategy implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreStrategy.class);

    public void handle(Message msg, Consumer consumer) {
        try {
            LOGGER.info(String.format("Ignoring error for message; %s", msg.getValue().toString()));
            consumer.acknowledge(msg);
        } catch (final PulsarClientException ex) {
            LOGGER.error("Failed to ack message", ex);
        }
    }
}