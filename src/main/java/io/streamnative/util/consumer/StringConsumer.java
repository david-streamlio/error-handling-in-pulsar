package io.streamnative.util.consumer;

import io.streamnative.util.consumer.handlers.success.WriteToTopicStrategy;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class StringConsumer extends AbstractConsumer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringConsumer.class);

    public StringConsumer(PulsarClient client, String srcTopicName, String subscriptionName) {
        super(client, srcTopicName, subscriptionName);
    }

    protected ConsumerBuilder<String> createConsumerBuilder() {
        return pulsarClient.newConsumer(Schema.STRING)
                .topic(topicName)
                .subscriptionName(subscriptionName);
    }

}