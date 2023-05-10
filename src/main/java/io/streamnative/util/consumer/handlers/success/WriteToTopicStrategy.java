package io.streamnative.util.consumer.handlers.success;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
public class WriteToTopicStrategy<T> implements MessageHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteToTopicStrategy.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    private PulsarAdmin pulsarAdmin;

    private String destTopicName;
    private Producer<T> producer;

    public WriteToTopicStrategy(String topic) {
        this.destTopicName = topic;
    }

    @Override
    public void handleMessage(Message<T> msg, Consumer<T> consumer) throws PulsarClientException {

        long processingTime = System.currentTimeMillis() - msg.getPublishTime();
        try {
            getProducer().newMessage()
                    .key(msg.getKey())
                    .value(msg.getValue())
                    .property(MessageHandler.PROCESSING_TIME, Long.toString(processingTime))
                    .property(MessageHandler.ORIGINAL_PUBLISH_TIME, Long.toString(msg.getPublishTime()))
                    .send();

            consumer.acknowledge(msg);

            LOGGER.debug(String.format("Successfully processed message [value: %s, Publish Time: %d]]",
                    msg.getValue(), msg.getPublishTime()));

        } catch (PulsarClientException e) {
            LOGGER.error("Can't write message", e);
        }
    }

    private Producer<T> getProducer() {
        if (producer == null) {
            try {
                LOGGER.debug(String.format("Creating Producer on topic [%s]", destTopicName));
                producer = (Producer<T>) pulsarClient.newProducer(Schema.STRING).topic(destTopicName).create();
            } catch (PulsarClientException e) {
                throw new RuntimeException(e);
            }
        }
        return producer;
    }

    @Override
    public void close() throws IOException {
        if (producer != null && producer.isConnected()) {
            producer.close();
            producer = null;
        }
    }
}
