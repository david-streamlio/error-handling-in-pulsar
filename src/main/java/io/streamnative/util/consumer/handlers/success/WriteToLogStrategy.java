package io.streamnative.util.consumer.handlers.success;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WriteToLogStrategy<T> implements MessageHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteToTopicStrategy.class);

    @Override
    public void handleMessage(Message<T> msg, Consumer<T> consumer) throws PulsarClientException {
        if (msg.hasProperty("Processing time")) {
            LOGGER.info(msg.getProperty("Processing time"));
        }
        LOGGER.info(String.format("Received Message [value : %s, Original Publish Time: %d, Processing Time: %s",
                msg.getValue().toString(), msg.getPublishTime(),
                msg.hasProperty(MessageHandler.PROCESSING_TIME) ?
                        msg.getProperty(MessageHandler.PROCESSING_TIME) : "Unknown"));

        consumer.acknowledge(msg);
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }
}
