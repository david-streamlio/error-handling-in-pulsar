package io.streamnative.util.producer;

import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Scope("prototype")
public class StringProducer extends AbstractProducer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringProducer.class);

    private Integer messageCounter = Integer.valueOf(1);

    public StringProducer(PulsarClient client, String topicName) throws PulsarClientException {
        super(client, topicName);
    }

    protected Producer<String> createProducer() {
        try {
            return pulsarClient.newProducer(Schema.STRING)
                    .topic(topicName)
                    .create();
        } catch (PulsarClientException e) {
            LOGGER.error("Failed to create producer", e);
            return null;
        }
    }


    // our (very simple) message content builder
    protected String generateMessageContent() {
        String msg = messageCounter.toString();
        LOGGER.info("Sending message: {}", msg);
        messageCounter++;
        return msg;
    }

}