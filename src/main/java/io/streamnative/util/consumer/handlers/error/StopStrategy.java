package io.streamnative.util.consumer.handlers.error;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopStrategy implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopStrategy.class);

    @Override
    public void handle(Message msg, Consumer consumer) {
        LOGGER.info(String.format("Stopping Processing on message [value: %s]", msg.getValue()));
        consumer.pause();
    }
}
