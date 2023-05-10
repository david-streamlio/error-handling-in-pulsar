package io.streamnative.util.reader;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.TimeUnit;

public class StringTopicReader extends AbstractTopicReader<String> {

    public StringTopicReader(PulsarClient pulsarClient, String topicName) {
        super(pulsarClient, topicName);
    }

    @Override
    public Reader<String> getReader(long startTime) throws PulsarClientException {
        if (reader == null) {
            reader = pulsarClient.newReader(Schema.STRING)
                    .topic(topicName)
                    .startMessageFromRollbackDuration((System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS)
                    .create();
        }
        return reader;
    }

}
