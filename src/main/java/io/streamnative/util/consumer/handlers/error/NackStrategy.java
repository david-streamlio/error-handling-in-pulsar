package io.streamnative.util.consumer.handlers.error;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NackStrategy implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NackStrategy.class);

    @Override
    public void handle(Message msg, Consumer consumer) {
        LOGGER.info(String.format("Nacking message [value: %s, Publish Time: %d]",
                msg.getValue().toString(), msg.getPublishTime()));

        consumer.negativeAcknowledge(msg);
    }
}
